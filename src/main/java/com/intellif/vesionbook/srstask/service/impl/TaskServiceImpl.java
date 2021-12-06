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
import com.intellif.vesionbook.srstask.model.vo.req.CreateTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.DestroyTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Time;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


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
    public BaseResponseVo<CreateTaskRspVo> createStreamTask(CreateTaskReqVo createTaskReqVo) {

        String app = createTaskReqVo.getApp();
        String uniqueId = createTaskReqVo.getUniqueId();
        String originStream = createTaskReqVo.getOriginStream();
        Integer outputType = createTaskReqVo.getOutputType();

        if(outputType != StreamOutputTypeEnum.RTMP.getCode() && outputType != StreamOutputTypeEnum.HTTP_HLV.getCode()
                && outputType != StreamOutputTypeEnum.WEB_RTC.getCode()) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }

        BaseResponseVo<CreateTaskRspVo> ret = getStreamAddress(app, uniqueId, outputType);
        if (ret != null) {
            return ret;
        }

        Process ffmpeg = ffManagementHelper.transcodeToRtmpAndHlvAndRTC(originStream, app, uniqueId);
        StreamTaskCache.storeProcess(app, uniqueId, ffmpeg);


        String rtmpOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.RTMP.getCode());
        String httpFlvOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.HTTP_HLV.getCode());
        String webrtcOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.WEB_RTC.getCode());

        StreamTask task = StreamTask.builder().app(app).uniqueId(uniqueId).originStream(originStream)
                .service(serverConfig.getServiceHost()).status(StreamTaskStatusEnum.INIT.getCode())
                .rtmpOutput(rtmpOutput).httpFlvOutput(httpFlvOutput).webrtcOutput(webrtcOutput).build();

        log.info("task: {}", task);

        streamTaskMapper.insertSelective(task);

        CreateTaskRspVo vo;
        if(outputType == StreamOutputTypeEnum.RTMP.getCode()){
            vo = CreateTaskRspVo.builder().rtmpOutput(rtmpOutput).build();
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode()){
            vo = CreateTaskRspVo.builder().rtmpOutput(httpFlvOutput).build();
        } else {
            vo = CreateTaskRspVo.builder().rtmpOutput(webrtcOutput).build();
        }
        log.info("start");
        return BaseResponseVo.ok(vo);
    }

    public String getOutputStream(String app, String uniqueId, Integer outputType) {
        String output;
        if(outputType == StreamOutputTypeEnum.RTMP.getCode()) {
            output = "rtmp://" + serverConfig.getOutputHost() +"/" + app + "/" + uniqueId;
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode()){
            output = "http://" + serverConfig.getOutputHost() + ":" + serverConfig.getHttpFlvPort() + "/" + app + "/" + uniqueId + ".flv";
        } else if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode()){
            output = "webrtc://" + serverConfig.getOutputHost() +"/" + app + "/" + uniqueId;
        } else {
            return null;
        }
        return output;
    }

    public BaseResponseVo<CreateTaskRspVo> getStreamAddress(String app, String uniqueId, Integer outputType) {
        BaseResponseVo<StreamTask> ret = getStreamTask(app, uniqueId, true);
        if(ret == null){
            return null;
        }
        if(!ret.isSuccess()) {
            return BaseResponseVo.error(ret.getRespCode(), ret.getRespMark(), ret.getRespMessage());
        }
        StreamTask task = ret.getData();


        CreateTaskRspVo vo;
        if(outputType == StreamOutputTypeEnum.RTMP.getCode() && !StringUtils.isEmpty(task.getRtmpOutput())){
            vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode() && !StringUtils.isEmpty(task.getHttpFlvOutput())){
            vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
        } else if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode() && !StringUtils.isEmpty(task.getWebrtcOutput())){
            vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
        } else {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }
        return BaseResponseVo.ok(vo);

    }

    public BaseResponseVo<StreamTask> getStreamTask(String app, String uniqueId, Boolean lock) {
        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId).lock(lock).build();
        List<StreamTask> streamTasks = streamTaskMapper.selectByParam(streamTaskDto);
        if(streamTasks==null) {
            return null;
        }

        streamTasks = streamTasks.stream().filter(item -> item.getStatus() != StreamTaskStatusEnum.CLOSED.getCode()).collect(Collectors.toList());
        if(streamTasks.size()>1) {
            log.error("发现重复的流任务 stream tasks: {}", streamTasks);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_REPEAT);
        }
        if(streamTasks.size()==0) {
            return null;
        }

        return BaseResponseVo.ok(streamTasks.get(0));
    }


    @Override
    @Transactional
    public BaseResponseVo<String> deleteStreamTask(DestroyTaskReqVo destroyTaskReqVo) {
        String app = destroyTaskReqVo.getApp();
        String uniqueId = destroyTaskReqVo.getUniqueId();
        String originStream = destroyTaskReqVo.getOriginStream();

        BaseResponseVo<StreamTask> ret = getStreamTask(app, uniqueId, true);
        if(ret == null) {
            log.error("数据库未发现该流任务 app: {}, uniqueId: {}, originStream: {}", app, uniqueId, originStream);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_DATABASE_NOT_FOUND);
        }
        if(!ret.isSuccess()) {
            return BaseResponseVo.error(ret.getRespCode(), ret.getRespMark(), ret.getRespMessage());
        }
        Process process = StreamTaskCache.getProcess(app, uniqueId);
        if(process == null) {
            log.error("缓存中未发现该流任务 app: {}, uniqueId: {}, originStream: {}", app, uniqueId, originStream);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_DATABASE_NOT_FOUND);
        }
        process.destroy();

        // 关闭
        StreamTask task = StreamTask.builder().app(app).uniqueId(uniqueId).status(StreamTaskStatusEnum.CLOSED.getCode()).build();
        streamTaskMapper.updateStatus(task);

        return BaseResponseVo.ok();
    }
}
