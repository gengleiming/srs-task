package com.intellif.vesionbook.srstask.service.impl;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.enums.StreamOutputTypeEnum;
import com.intellif.vesionbook.srstask.enums.StreamTaskStatusEnum;
import com.intellif.vesionbook.srstask.helper.FFManagementHelper;
import com.intellif.vesionbook.srstask.mapper.StreamTaskMapper;
import com.intellif.vesionbook.srstask.model.dto.StreamTaskDto;
import com.intellif.vesionbook.srstask.model.entity.StreamTask;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.GetOrCreateTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Resource
    FFManagementHelper ffManagementHelper;

    @Resource
    StreamTaskMapper streamTaskMapper;

    @Resource
    ServerConfig serverConfig;

    @Override
    @Transactional
    public BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(GetOrCreateTaskReqVo getOrCreateTaskReqVo) {

        String app = getOrCreateTaskReqVo.getApp();
        String uniqueId = getOrCreateTaskReqVo.getUniqueId();
        String originStream = getOrCreateTaskReqVo.getOriginStream();
        Integer outputType = getOrCreateTaskReqVo.getOutputType();
        Boolean forever = Optional.ofNullable(getOrCreateTaskReqVo.getForever()).orElse(false);

        if (outputType != StreamOutputTypeEnum.RTMP.getCode() && outputType != StreamOutputTypeEnum.HTTP_HLV.getCode()
                && outputType != StreamOutputTypeEnum.WEB_RTC.getCode() && outputType != StreamOutputTypeEnum.HLS.getCode()) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }

        BaseResponseVo<StreamTask> ret = getAliveStreamTask(app, uniqueId, true);
        if (ret != null && !ret.isSuccess()) {
            return BaseResponseVo.error(ret.getRespCode(), ret.getRespMark(), ret.getRespMessage());
        }

        if (ret != null && ret.isSuccess()) {
            StreamTask task = ret.getData();
            Process process = StreamTaskCache.getProcess(task.getApp(), task.getUniqueId());
            log.info("app: {}, unique id: {}, process is null: {}, process is alive: {}", app, uniqueId,
                    process == null, process != null && process.isAlive());
            if (process != null && process.isAlive()) {
                return getStreamAddress(task, outputType);
            }
        }

        // 创建任务
        Process ffmpeg = ffManagementHelper.transcodeStream(originStream, app, uniqueId);
        if (ffmpeg == null) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_FAILED);
        }

        // 缓存ffmpeg对象
        StreamTaskCache.storeProcess(app, uniqueId, ffmpeg);

        String rtmpOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.RTMP.getCode());
        String httpFlvOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.HTTP_HLV.getCode());
        String webrtcOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.WEB_RTC.getCode());
        String hlsOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.HLS.getCode());
        StreamTask task = StreamTask.builder().app(app).uniqueId(uniqueId).originStream(originStream)
                .service(serverConfig.getServiceHost()).status(StreamTaskStatusEnum.PROCESSING.getCode())
                .rtmpOutput(rtmpOutput).httpFlvOutput(httpFlvOutput).hlsOutput(hlsOutput).webrtcOutput(webrtcOutput)
                .forever(forever).build();

        if (ret == null) {
            streamTaskMapper.insertSelective(task);
        }

        return getStreamAddress(task, outputType);
    }

    public String getOutputStream(String app, String uniqueId, Integer outputType) {
        String output;
        if (outputType == StreamOutputTypeEnum.RTMP.getCode()) {
            output = "rtmp://" + serverConfig.getOutputHost() + "/" + app + "/" + uniqueId;
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode()) {
            output = "http://" + serverConfig.getOutputHost() + ":" + serverConfig.getHttpOutputPort() + "/" + app + "/" + uniqueId + ".flv";
        } else if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode()) {
            output = "webrtc://" + serverConfig.getOutputHost() + "/" + app + "/" + uniqueId;
        } else if (outputType == StreamOutputTypeEnum.HLS.getCode()) {
            output = "http://" + serverConfig.getOutputHost() + ":" + serverConfig.getHttpOutputPort() + "/" + app + "/" + uniqueId + ".m3u8";
        } else {
            return null;
        }
        return output;
    }

    public BaseResponseVo<CreateTaskRspVo> getStreamAddress(StreamTask task, Integer outputType) {

        CreateTaskRspVo vo;
        if (outputType == StreamOutputTypeEnum.RTMP.getCode() && !StringUtils.isEmpty(task.getRtmpOutput())) {
            vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode() && !StringUtils.isEmpty(task.getHttpFlvOutput())) {
            vo = CreateTaskRspVo.builder().httpFlvOutput(task.getHttpFlvOutput()).build();
        } else if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode() && !StringUtils.isEmpty(task.getWebrtcOutput())) {
            vo = CreateTaskRspVo.builder().webrtcOutput(task.getWebrtcOutput()).build();
        } else if (outputType == StreamOutputTypeEnum.HLS.getCode() && !StringUtils.isEmpty(task.getHlsOutput())) {
            vo = CreateTaskRspVo.builder().hlsOutput(task.getHlsOutput()).build();
        } else {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }
        return BaseResponseVo.ok(vo);
    }

    public BaseResponseVo<StreamTask> getAliveStreamTask(String app, String uniqueId, Boolean lock) {
        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId)
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).lock(lock).build();
        List<StreamTask> streamTasks = streamTaskMapper.selectByParam(streamTaskDto);

        if (streamTasks == null) {
            return null;
        }

        if (streamTasks.size() > 1) {
            log.error("发现重复的流任务 stream tasks: {}", streamTasks);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_REPEAT);
        }
        if (streamTasks.size() == 0) {
            return null;
        }

        return BaseResponseVo.ok(streamTasks.get(0));
    }


    @Override
    @Transactional
    public BaseResponseVo<String> closeStreamTask(CloseTaskReqVo closeTaskReqVo) {
        String app = closeTaskReqVo.getApp();
        String uniqueId = closeTaskReqVo.getUniqueId();
        String originStream = closeTaskReqVo.getOriginStream();

        BaseResponseVo<StreamTask> ret = getAliveStreamTask(app, uniqueId, true);
        if (ret == null) {
            log.error("数据库未发现该流任务 app: {}, uniqueId: {}, originStream: {}", app, uniqueId, originStream);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_DATABASE_NOT_FOUND);
        }
        if (!ret.isSuccess()) {
            return BaseResponseVo.error(ret.getRespCode(), ret.getRespMark(), ret.getRespMessage());
        }
        Process process = StreamTaskCache.getProcess(app, uniqueId);
        if (process == null) {
            log.error("缓存中未发现该流任务 app: {}, uniqueId: {}, originStream: {}", app, uniqueId, originStream);
        } else {
            process.destroy();
        }

        // 关闭
        StreamTask task = StreamTask.builder().app(app).uniqueId(uniqueId).status(StreamTaskStatusEnum.CLOSED.getCode()).build();
        streamTaskMapper.updateStatus(task);

        return BaseResponseVo.ok();
    }
}
