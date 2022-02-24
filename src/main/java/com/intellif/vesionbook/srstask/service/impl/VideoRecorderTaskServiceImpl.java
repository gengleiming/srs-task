package com.intellif.vesionbook.srstask.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.intellif.vesionbook.srstask.cache.VideoRecorderTaskCache;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.enums.StreamOutputTypeEnum;
import com.intellif.vesionbook.srstask.enums.StreamTypeEnum;
import com.intellif.vesionbook.srstask.enums.VideoRecorderTaskStatusEnum;
import com.intellif.vesionbook.srstask.helper.FFCommandHelper;
import com.intellif.vesionbook.srstask.mapper.VideoRecorderTaskMapper;
import com.intellif.vesionbook.srstask.model.dto.UpdateStatusVideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.dto.VideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.entity.VideoRecorderTask;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.TimeRange;
import com.intellif.vesionbook.srstask.model.vo.req.VideoRecorderTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import com.intellif.vesionbook.srstask.service.VideoRecorderTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoRecorderTaskServiceImpl implements VideoRecorderTaskService {
    @Resource
    VideoRecorderTaskMapper videoRecorderTaskMapper;
    @Resource
    TaskService taskService;
    @Resource
    FFCommandHelper ffCommandHelper;
    @Resource
    VideoRecorderTaskCache videoRecorderTaskCache;
    @Resource
    ServerConfig serverConfig;

    @Override
    public BaseResponseVo<String> create(VideoRecorderTaskReqVo reqVo) {
        log.info("create recorder task req: {}", reqVo);
        if (StringUtils.isEmpty(reqVo.getChannelId()) && StringUtils.isEmpty(reqVo.getOriginStream())) {
            log.error("param invalid all null. origin stream: {}, channel id: {}",
                    reqVo.getOriginStream(), reqVo.getChannelId());
            return BaseResponseVo.error(ReturnCodeEnum.PARAM_INVALID);
        }

        if (!StringUtils.isEmpty(reqVo.getChannelId()) && !StringUtils.isEmpty(reqVo.getOriginStream())) {
            log.error("param invalid all not null. origin stream: {}, channel id: {}",
                    reqVo.getOriginStream(), reqVo.getChannelId());
            return BaseResponseVo.error(ReturnCodeEnum.PARAM_INVALID);
        }

        int streamType;
        if (!StringUtils.isEmpty(reqVo.getChannelId())) {
            streamType = StreamTypeEnum.GB28181.getCode();
        } else {
            streamType = StreamTypeEnum.RTSP.getCode();
        }

        List<VideoRecorderTask> videoRecorderTasks = new ArrayList<>();
        for (TimeRange timeRange : reqVo.getTimeRangeList()) {
            VideoRecorderTask model = new VideoRecorderTask();
            BeanUtils.copyProperties(reqVo, model);
            model.setStatus(VideoRecorderTaskStatusEnum.INIT.getCode());
            model.setStartTime(timeRange.getStartTime());
            model.setEndTime(timeRange.getEndTime());
            model.setStreamType(streamType);

            videoRecorderTasks.add(model);
        }
        videoRecorderTaskMapper.insertSelective(videoRecorderTasks);
        return BaseResponseVo.ok();
    }

    @Override
    public VideoRecorderTask selectById(String id) {
        return videoRecorderTaskMapper.selectById(id);
    }

    @Override
    public PageInfo<VideoRecorderTask> getList(VideoRecorderTaskDto videoRecorderTaskDto) {
        PageHelper.startPage(videoRecorderTaskDto.getPage(), videoRecorderTaskDto.getPageSize());
        List<VideoRecorderTask> list = videoRecorderTaskMapper.selectByParam(videoRecorderTaskDto);
        return new PageInfo<>(list);
    }

    @Override
    public void videoRecordStart() {
        VideoRecorderTaskDto recorderTaskDto = VideoRecorderTaskDto.builder().page(1).pageSize(10000)
                .status(VideoRecorderTaskStatusEnum.INIT.getCode()).build();

        PageInfo<VideoRecorderTask> list = getList(recorderTaskDto);
        List<VideoRecorderTask> taskList = list.getList();

        long timestamp = System.currentTimeMillis() / 1000;
        // 找到开始录制的流
        List<VideoRecorderTask> prepareList = taskList.stream().filter(item -> item.getStartTime() <= timestamp)
                .collect(Collectors.toList());

        log.info("prepare start recorder list: {}", prepareList);

        // 开始录制
        BaseResponseVo<CreateTaskRspVo> streamResult;
        List<Long> idList = new ArrayList<>();
        for (VideoRecorderTask task : prepareList) {
            TaskReqVo taskReqVo = TaskReqVo.builder().app(task.getApp()).uniqueId(task.getUniqueId())
                    .originStream(task.getOriginStream()).channelId(task.getChannelId())
                    .outputType(StreamOutputTypeEnum.RTMP.getCode()).build();

            String uniqueId;
            if (task.getStreamType() == StreamTypeEnum.GB28181.getCode()) {
                streamResult = taskService.getGBStream(taskReqVo);
                uniqueId = task.getUniqueId() + "@" + task.getChannelId();
            } else {
                streamResult = taskService.getOrCreateStreamTask(taskReqVo);
                uniqueId = task.getUniqueId();
            }

            if (!streamResult.isSuccess()) {
                log.error("video recorder. get rtsp stream result error: {}", streamResult);
                return;
            }

            String rtmpOutput = streamResult.getData().getRtmpOutput();
            // 推流，由srs自动录制
            log.info("start create cache video recorder task. recorder task id: {}, app: {}, unique id: {}, " +
                            "origin stream: {}", task.getId(), task.getApp(), uniqueId, rtmpOutput);
            createVideoRecorderProcess(rtmpOutput, task.getApp(), uniqueId);
            // 更新数据库状态，录制中
            idList.add(task.getId());
        }

        UpdateStatusVideoRecorderTaskDto updateDto = new UpdateStatusVideoRecorderTaskDto();
        updateDto.setStatus(VideoRecorderTaskStatusEnum.RUNNING.getCode());
        updateDto.setIdList(idList);
        videoRecorderTaskMapper.updateStatus(updateDto);
        log.info("update video recorder RUNNING. id list: {}", idList);
    }

    public void createVideoRecorderProcess(String originStream, String app, String uniqueId) {

        // 创建流任务
        Process ffmpeg = ffCommandHelper.transcodeStream(originStream, app, uniqueId, serverConfig.getSrsRecorderHost());
        if (ffmpeg == null) {
            return;
        }
        videoRecorderTaskCache.storeProcess(app, uniqueId, ffmpeg);
    }

    @Override
    public void videoRecordStop() {
        VideoRecorderTaskDto recorderTaskDto = VideoRecorderTaskDto.builder().page(1).pageSize(10000)
                .status(VideoRecorderTaskStatusEnum.RUNNING.getCode()).build();

        PageInfo<VideoRecorderTask> list = getList(recorderTaskDto);
        List<VideoRecorderTask> taskList = list.getList();

        long timestamp = System.currentTimeMillis() / 1000;
        // 找到开始录制的流
        List<VideoRecorderTask> prepareList = taskList.stream().filter(item -> item.getEndTime() <= timestamp)
                .collect(Collectors.toList());

        log.info("prepare stop recorder list: {}", prepareList);

        // 结束录制
        List<Long> idList = new ArrayList<>();
        for (VideoRecorderTask task : prepareList) {
            String uniqueId;
            if (task.getStreamType() == StreamTypeEnum.GB28181.getCode()) {
                uniqueId = task.getUniqueId() + "@" + task.getChannelId();
            } else {
                uniqueId = task.getUniqueId();
            }
            // 停止推流，由srs自动结束录制
            log.info("stop video recorder task. recorder task id: {}, app: {}, unique id: {}",
                    task.getId(), task.getApp(), uniqueId);
            stopVideoRecorderProcess(task.getApp(), uniqueId);
            // 更新数据库状态，结束录制
            idList.add(task.getId());
        }

        UpdateStatusVideoRecorderTaskDto updateDto = new UpdateStatusVideoRecorderTaskDto();
        updateDto.setStatus(VideoRecorderTaskStatusEnum.FINISHED.getCode());
        updateDto.setIdList(idList);
        videoRecorderTaskMapper.updateStatus(updateDto);
        log.info("update video recorder RUNNING. id list: {}", idList);
    }

    public void stopVideoRecorderProcess(String app, String uniqueId) {
        Process process = videoRecorderTaskCache.getProcess(app, uniqueId);
        if(process == null || !process.isAlive()) {
            log.error("stop video recorder error, process not alive. app: {}, unique id: {}, process: {}",
                    app, uniqueId, process);
        } else {
            process.destroy();
        }
        videoRecorderTaskCache.clearProcess(app, uniqueId);
    }

}
