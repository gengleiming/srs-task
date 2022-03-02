package com.intellif.vesionbook.srstask.service.impl;

import com.aliyun.oss.OSS;
import com.intellif.vesionbook.srstask.cache.VideoRecorderTaskCache;
import com.intellif.vesionbook.srstask.config.OssConfig;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.enums.StreamOutputTypeEnum;
import com.intellif.vesionbook.srstask.helper.ClientCallbackHelper;
import com.intellif.vesionbook.srstask.helper.FFCommandHelper;
import com.intellif.vesionbook.srstask.helper.OssHelper;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.*;
import com.intellif.vesionbook.srstask.model.vo.rsp.StreamTaskRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import com.intellif.vesionbook.srstask.service.VideoRecorderTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VideoRecorderTaskServiceImpl implements VideoRecorderTaskService {
    @Resource
    TaskService taskService;
    @Resource
    FFCommandHelper ffCommandHelper;
    @Resource
    VideoRecorderTaskCache videoRecorderTaskCache;
    @Resource
    ServerConfig serverConfig;
    @Resource
    OssHelper ossHelper;
    @Resource
    OssConfig ossConfig;
    @Resource
    ClientCallbackHelper clientCallbackHelper;

//    @Override
//    public BaseResponseVo<String> create(VideoRecorderTaskReqVo reqVo) {
//        log.info("create recorder task req: {}", reqVo);
//        if (StringUtils.isEmpty(reqVo.getChannelId()) && StringUtils.isEmpty(reqVo.getOriginStream())) {
//            log.error("param invalid all null. origin stream: {}, channel id: {}",
//                    reqVo.getOriginStream(), reqVo.getChannelId());
//            return BaseResponseVo.error(ReturnCodeEnum.PARAM_INVALID);
//        }
//
//        if (!StringUtils.isEmpty(reqVo.getChannelId()) && !StringUtils.isEmpty(reqVo.getOriginStream())) {
//            log.error("param invalid all not null. origin stream: {}, channel id: {}",
//                    reqVo.getOriginStream(), reqVo.getChannelId());
//            return BaseResponseVo.error(ReturnCodeEnum.PARAM_INVALID);
//        }
//
//        int streamType;
//        if (!StringUtils.isEmpty(reqVo.getChannelId())) {
//            streamType = StreamTypeEnum.GB28181.getCode();
//        } else {
//            streamType = StreamTypeEnum.RTSP.getCode();
//        }
//
//        List<VideoRecorderTask> videoRecorderTasks = new ArrayList<>();
//        for (TimeRange timeRange : reqVo.getTimeRangeList()) {
//            VideoRecorderTask model = new VideoRecorderTask();
//            BeanUtils.copyProperties(reqVo, model);
//            model.setStatus(VideoRecorderTaskStatusEnum.INIT.getCode());
//            model.setStartTime(timeRange.getStartTime());
//            model.setEndTime(timeRange.getEndTime());
//            model.setStreamType(streamType);
//
//            videoRecorderTasks.add(model);
//        }
//        videoRecorderTaskMapper.insertBatch(videoRecorderTasks);
//        return BaseResponseVo.ok();
//    }
//
//    @Override
//    public VideoRecorderTask selectById(Long id) {
//        return videoRecorderTaskMapper.selectById(id);
//    }

//    @Override
//    public PageInfo<VideoRecorderTaskListVo> getList(VideoRecorderTaskDto videoRecorderTaskDto, boolean withShareUrl) {
//        PageHelper.startPage(videoRecorderTaskDto.getPage(), videoRecorderTaskDto.getPageSize());
//        List<VideoRecorderTaskListVo> list = videoRecorderTaskMapper.selectByParam(videoRecorderTaskDto);
//
//        if (withShareUrl) {
//            OSS ossStsCredentialsClient = ossHelper.getOssStsCredentialsClient();
//            for (VideoRecorderTaskListVo task : list) {
//                String ossObjectName = task.getOssObjectName();
//                if (!StringUtils.isEmpty(ossObjectName)) {
//                    BaseResponseVo<String> ret = ossHelper.getOssUrl(ossObjectName, ossStsCredentialsClient);
//                    if (ret.isSuccess()) {
//                        task.setOssUrl(ret.getData());
//                    } else {
//                        task.setOssUrl(null);
//                    }
//                }
//            }
//            ossStsCredentialsClient.shutdown();
//        }
//        return new PageInfo<>(list);
//    }

//    @Override
//    public void videoRecordStart() {
//        VideoRecorderTaskDto recorderTaskDto = VideoRecorderTaskDto.builder().page(1).pageSize(10000)
//                .status(VideoRecorderTaskStatusEnum.INIT.getCode()).build();
//
//        PageInfo<VideoRecorderTaskListVo> list = getList(recorderTaskDto, false);
//        List<VideoRecorderTaskListVo> taskList = list.getList();
//
//        long timestamp = System.currentTimeMillis() / 1000;
//        // 找到开始录制的任务
//        List<VideoRecorderTaskListVo> prepareList = taskList.stream().filter(item -> item.getStartTime() <= timestamp &&
//                item.getEndTime() > timestamp).collect(Collectors.toList());
//        // 找到超时录制的任务
//        List<VideoRecorderTaskListVo> expireList = taskList.stream().filter(item -> item.getEndTime() <= timestamp)
//                .collect(Collectors.toList());
//
//        // 开始录制
//        if (!prepareList.isEmpty()) {
//            log.info("start recorder list: {}", prepareList);
//            BaseResponseVo<StreamTaskRspVo> streamResult;
//            for (VideoRecorderTaskListVo task : prepareList) {
//                TaskReqVo taskReqVo = TaskReqVo.builder().app(task.getApp()).uniqueId(task.getUniqueId())
//                        .originStream(task.getOriginStream()).channelId(task.getChannelId())
//                        .outputType(StreamOutputTypeEnum.RTMP.getCode()).build();
//
//                String uniqueId;
//                if (task.getStreamType() == StreamTypeEnum.GB28181.getCode()) {
//                    streamResult = taskService.getGBStream(taskReqVo);
//                    uniqueId = task.getUniqueId() + "@" + task.getChannelId();
//                } else {
//                    streamResult = taskService.getOrCreateStreamTask(taskReqVo);
//                    uniqueId = task.getUniqueId();
//                }
//                uniqueId = uniqueId + "/" + task.getId();
//
//                if (!streamResult.isSuccess()) {
//                    log.error("video recorder. get rtsp stream result error: {}", streamResult);
//                    return;
//                }
//
//                String rtmpOutput = streamResult.getData().getRtmpOutput();
//                // 推流，由srs自动录制
//                log.info("start create cache video recorder task. recorder task id: {}, app: {}, unique id: {}, " +
//                        "origin stream: {}", task.getId(), task.getApp(), uniqueId, rtmpOutput);
//                createVideoRecorderProcess(String.valueOf(task.getId()), rtmpOutput, task.getApp(), uniqueId);
//            }
//
//            UpdateVideoRecorderTaskDto updateDto = new UpdateVideoRecorderTaskDto();
//            updateDto.setStatus(VideoRecorderTaskStatusEnum.RUNNING.getCode());
//            List<Long> idList = prepareList.stream().map(VideoRecorderTaskListVo::getId).collect(Collectors.toList());
//            updateDto.setIdList(idList);
//            videoRecorderTaskMapper.updateStatus(updateDto);
//            log.info("update video recorder RUNNING. id list: {}", idList);
//        }
//
//        if (!expireList.isEmpty()) {
//            log.info("start recorder expire list: {}", expireList);
//            UpdateVideoRecorderTaskDto updateDto = new UpdateVideoRecorderTaskDto();
//            updateDto.setStatus(VideoRecorderTaskStatusEnum.EXPIRE.getCode());
//            List<Long> idList = expireList.stream().map(VideoRecorderTaskListVo::getId).collect(Collectors.toList());
//            updateDto.setIdList(idList);
//            videoRecorderTaskMapper.updateStatus(updateDto);
//            log.info("update video recorder EXPIRE. id list: {}", idList);
//        }
//    }

    public void createVideoRecorderProcess(String taskId, String originStream, String app, String uniqueId) {

        String recorderParam = "?taskId=" + taskId;
        // 创建流任务
        Process ffmpeg = ffCommandHelper.transcodeStream(originStream, app, uniqueId, serverConfig.getSrsRecorderHost(), recorderParam);
        if (ffmpeg == null) {
            log.error("start video recorder error. task id: {}, origin stream: {}, app: {}, unique id: {}",
                    taskId, originStream, app, uniqueId);
            return;
        }
        videoRecorderTaskCache.storeProcess(app, uniqueId, ffmpeg);
    }

//    @Override
//    public void videoRecordStop() {
//        VideoRecorderTaskDto recorderTaskDto = VideoRecorderTaskDto.builder().page(1).pageSize(10000)
//                .status(VideoRecorderTaskStatusEnum.RUNNING.getCode()).build();
//
//        PageInfo<VideoRecorderTaskListVo> list = getList(recorderTaskDto, false);
//        List<VideoRecorderTaskListVo> taskList = list.getList();
//
//        long timestamp = System.currentTimeMillis() / 1000;
//        // 找到正在录制的流
//        List<VideoRecorderTaskListVo> prepareList = taskList.stream().filter(item -> item.getEndTime() <= timestamp)
//                .collect(Collectors.toList());
//
//        if (prepareList.isEmpty()) {
//            return;
//        }
//
//        log.info("prepare stop recorder list: {}", prepareList);
//
//        // 结束录制
//        List<Long> idList = new ArrayList<>();
//        List<Long> errorList = new ArrayList<>();
//        for (VideoRecorderTaskListVo task : prepareList) {
//            String uniqueId;
//            if (task.getStreamType() == StreamTypeEnum.GB28181.getCode()) {
//                uniqueId = task.getUniqueId() + "@" + task.getChannelId();
//            } else {
//                uniqueId = task.getUniqueId();
//            }
//            uniqueId = uniqueId + "/" + task.getId();
//            // 停止推流，由srs自动结束录制
//            log.info("stop video recorder task. recorder task id: {}, app: {}, unique id: {}",
//                    task.getId(), task.getApp(), uniqueId);
//            BaseResponseVo<String> result = stopVideoRecorderProcess(task.getApp(), uniqueId);
//            if (result.isSuccess()) {
//                idList.add(task.getId());
//            } else {
//                errorList.add(task.getId());
//            }
//        }
//
//        if (!idList.isEmpty()) {
//            UpdateVideoRecorderTaskDto updateDto = new UpdateVideoRecorderTaskDto();
//            updateDto.setStatus(VideoRecorderTaskStatusEnum.FINISHED.getCode());
//            updateDto.setIdList(idList);
//            videoRecorderTaskMapper.updateStatus(updateDto);
//            log.info("update video recorder FINISHED. id list: {}", idList);
//        }
//        if (!errorList.isEmpty()) {
//            UpdateVideoRecorderTaskDto updateDto = new UpdateVideoRecorderTaskDto();
//            updateDto.setStatus(VideoRecorderTaskStatusEnum.ERROR.getCode());
//            updateDto.setIdList(errorList);
//            videoRecorderTaskMapper.updateStatus(updateDto);
//            log.info("update video recorder ERROR. error list: {}", errorList);
//        }
//    }

    public BaseResponseVo<String> stopVideoRecorderProcess(String app, String uniqueId) {
        Process process = videoRecorderTaskCache.getProcess(app, uniqueId);
        if (process == null) {
            log.error("stop video recorder error, process not exist. app: {}, unique id: {}, process: {}",
                    app, uniqueId, process);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_VIDEO_RECORDER_TASK_NOT_EXISTS);
        }
        if (!process.isAlive()) {
            log.error("stop video recorder error, process not alive. app: {}, unique id: {}, process: {}",
                    app, uniqueId, process);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_VIDEO_RECORDER_TASK_NOT_EXISTS);
        } else {
            process.destroy();
        }
        videoRecorderTaskCache.clearProcess(app, uniqueId);
        return BaseResponseVo.ok();
    }

    @Override
    public void dealOnDvr(SRSCallbackOnDvrVo vo) {
        String filePath = vo.getFile();
        String objectName = ossConfig.getBucketRoot() + filePath;
        String[] split = filePath.split("/");
        String filename = split[split.length - 1];
        boolean ok = ossHelper.uploadFile(filePath, objectName, filename);
        log.info("deal on dvr, upload file to oss. result: {}, file path: {}, object name: {}",
                ok, filePath, objectName);
        if (!ok) {
            log.error("deal on dvr, upload file to oss error, file path: {}, object name: {}", filePath, objectName);
            return;
        }

        // 如果设置了回调接口，那么把文件回调给客户端
        if (!StringUtils.isEmpty(serverConfig.getRecorderFileCallbackUrl())) {
            String param = vo.getParam();
            param = param.replace("?", "");
            String[] paramList = param.split("&");
            for (String item : paramList) {
                if (item.startsWith("taskId=")) {
                    String taskId = item.replace("taskId=", "");
                    RecorderFileClientCallbackVo clientCallbackVo = new RecorderFileClientCallbackVo();
                    clientCallbackVo.setTaskId(taskId);
                    clientCallbackVo.setObjectName(objectName);
                    clientCallbackVo.setFilePath(filePath);
                    clientCallbackHelper.recorderFileCallbackRequest(serverConfig.getRecorderFileCallbackUrl(), clientCallbackVo);
                }
            }
        }

    }

    @Override
    public void start(VideoRecorderReqVo vo) {
        TaskReqVo taskReqVo = TaskReqVo.builder().app(vo.getApp()).uniqueId(vo.getUniqueId())
                .originStream(vo.getOriginStream()).channelId(vo.getChannelId())
                .outputType(StreamOutputTypeEnum.RTMP.getCode()).build();

        BaseResponseVo<StreamTaskRspVo> result;
        String uniqueId;
        if (!StringUtils.isEmpty(vo.getChannelId())) {
            result = taskService.getGBStream(taskReqVo);
            uniqueId = vo.getUniqueId() + "@" + vo.getChannelId();
        } else {
            result = taskService.getOrCreateStreamTask(taskReqVo);
            uniqueId = vo.getUniqueId();
        }

        uniqueId = uniqueId + "/" + vo.getTaskId();

        if (!result.isSuccess()) {
            log.error("video recorder. get rtsp stream result error: {}", result);
            return;
        }

        String rtmpOutput = result.getData().getRtmpOutput();
        // 推流，由srs自动录制
        log.info("start create cache video recorder task. vo: {}, origin stream: {}", vo, rtmpOutput);
        createVideoRecorderProcess(vo.getTaskId(), rtmpOutput, vo.getApp(), uniqueId);
    }

    @Override
    public BaseResponseVo<String> stop(VideoRecorderReqVo vo) {

        String uniqueId;
        if (!StringUtils.isEmpty(vo.getChannelId())) {
            uniqueId = vo.getUniqueId() + "@" + vo.getChannelId();
        } else {
            uniqueId = vo.getUniqueId();
        }

        uniqueId = uniqueId + "/" + vo.getTaskId();

        // 停止推流，由srs自动结束录制
        return stopVideoRecorderProcess(vo.getApp(), uniqueId);
    }

    @Override
    public BaseResponseVo<GetOssUrlRspVo> getOssUrl(GetOssUrlReqVo vo) {
        GetOssUrlRspVo rspVo = new GetOssUrlRspVo();
        OSS ossStsCredentialsClient = ossHelper.getOssStsCredentialsClient();
        if (ossStsCredentialsClient == null) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_OSS_CLIENT_INIT_FAILED);
        }
        try {
            if (!StringUtils.isEmpty(vo.getObjectName())) {
                BaseResponseVo<String> ret = ossHelper.getOssUrl(vo.getObjectName(), ossStsCredentialsClient);

                if (ret.isSuccess()) {
                    rspVo.setOssUrl(ret.getData());
                } else {
                    rspVo.setOssUrl(null);
                }
            }

            List<String> objectNameList = vo.getObjectNameList();
            if (objectNameList != null && !objectNameList.isEmpty()) {
                List<String> list = new ArrayList<>();
                for (String objectName : objectNameList) {
                    BaseResponseVo<String> ret = ossHelper.getOssUrl(objectName, ossStsCredentialsClient);
                    if (ret.isSuccess()) {
                        list.add(ret.getData());
                    } else {
                        list.add(null);
                    }
                }
                rspVo.setOssUrlList(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ossStsCredentialsClient.shutdown();
        }

        return BaseResponseVo.ok(rspVo);
    }

}
