package com.intellif.vesionbook.srstask.service.impl;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.enums.StreamOutputTypeEnum;
import com.intellif.vesionbook.srstask.enums.StreamTaskStatusEnum;
import com.intellif.vesionbook.srstask.helper.FFCommandHelper;
import com.intellif.vesionbook.srstask.helper.JavaCVHelper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Resource
    StreamTaskMapper streamTaskMapper;

    @Resource
    ServerConfig serverConfig;

    @Resource
    StreamTaskCache streamTaskCache;

    @Resource
    JavaCVHelper javaCVHelper;

    @Resource
    FFCommandHelper ffCommandHelper;


    @Override
    @Transactional
    public BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(GetOrCreateTaskReqVo getOrCreateTaskReqVo) {

        String app = getOrCreateTaskReqVo.getApp();
        String uniqueId = getOrCreateTaskReqVo.getUniqueId();
        String originStream = getOrCreateTaskReqVo.getOriginStream();
        Integer outputType = getOrCreateTaskReqVo.getOutputType();
        Integer forever = Optional.ofNullable(getOrCreateTaskReqVo.getForever()).orElse(0);

        if (outputType != StreamOutputTypeEnum.RTMP.getCode() && outputType != StreamOutputTypeEnum.HTTP_HLV.getCode()
                && outputType != StreamOutputTypeEnum.WEB_RTC.getCode() && outputType != StreamOutputTypeEnum.HLS.getCode()) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }

        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId).service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).lock(true).build();
        List<StreamTask> tasks= getStreamTask(streamTaskDto);
        if (tasks.size() > 1) {
            log.error("发现重复的流任务 stream tasks: {}", tasks);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_REPEAT);
        }

        if (tasks.size() == 1) {
            StreamTask task = tasks.get(0);
            Boolean exists = checkCacheExistsOrClear(task);
            if(exists) {
                return getStreamAddress(task, outputType);
            }
        }

        // 创建任务
        createTask(originStream, app, uniqueId);
        Process ffmpeg = ffCommandHelper.transcodeStream(originStream, app, uniqueId);

        if (ffmpeg == null) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_FAILED);
        }

        String rtmpOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.RTMP.getCode());
        String httpFlvOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.HTTP_HLV.getCode());
        String webrtcOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.WEB_RTC.getCode());
        String hlsOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.HLS.getCode());
        StreamTask task = StreamTask.builder().app(app).uniqueId(uniqueId).originStream(originStream)
                .service(serverConfig.getServiceId()).status(StreamTaskStatusEnum.PROCESSING.getCode())
                .rtmpOutput(rtmpOutput).httpFlvOutput(httpFlvOutput).hlsOutput(hlsOutput).webrtcOutput(webrtcOutput)
                .forever(forever).build();

        if (tasks.size() == 0) {
            streamTaskMapper.insertSelective(task);
        }

        return getStreamAddress(task, outputType);
    }

    public Boolean checkCacheExistsOrClear(StreamTask task) {
        if(Objects.equals(serverConfig.getUseJavacv(), "1")) {
            Thread process = streamTaskCache.getThread(task.getApp(), task.getUniqueId());
            log.info("app: {}, unique id: {}, process is null: {}, process is alive: {}",
                    task.getApp(), task.getUniqueId(), process == null, process != null && process.isAlive());
            if (process != null ) {
                log.info("process state: {}", process.getState());
                if (process.isAlive()) {
                    return true;
                } else {
                    log.info("process exist but not alive");
                    process.interrupt();
                }
            }
            streamTaskCache.clearThread(task.getApp(), task.getUniqueId());
        } else{
            Process process = streamTaskCache.getProcess(task.getApp(), task.getUniqueId());
            log.info("app: {}, unique id: {}, process is null: {}, process is alive: {}",
                    task.getApp(), task.getUniqueId(), process == null, process != null && process.isAlive());
            if (process != null ) {
                if (process.isAlive()) {
                    return true;
                } else {
                    log.info("process exist but not alive");
                    process.destroy();
                }
            }
            streamTaskCache.clearProcess(task.getApp(), task.getUniqueId());
        }

        return null;
    }

    public void createTask(String originStream, String app, String uniqueId) {
        // 创建流任务
        if(serverConfig.getUseJavacv().equals("1")) {
            javaCVHelper.asyncPullRtspPushRtmp(originStream, app, uniqueId);
        } else {
            ffCommandHelper.transcodeStream(originStream, app, uniqueId);
        }
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

    @Override
    @Transactional
    public BaseResponseVo<String> closeStreamTask(CloseTaskReqVo closeTaskReqVo) {
        String app = closeTaskReqVo.getApp();
        String uniqueId = closeTaskReqVo.getUniqueId();
        String originStream = closeTaskReqVo.getOriginStream();

        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId).service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).lock(true).build();

        List<StreamTask> tasks = getStreamTask(streamTaskDto);

        if (tasks.isEmpty()) {
            log.error("数据库未发现该流任务 app: {}, uniqueId: {}, originStream: {}", app, uniqueId, originStream);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_DATABASE_NOT_FOUND);
        }

        if (tasks.size() > 1) {
            log.error("发现重复的流任务 stream tasks: {}", tasks);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_REPEAT);
        }

        StreamTask streamTask = tasks.get(0);
        checkCacheExistsOrClear(streamTask);

        // 关闭
        StreamTask task = StreamTask.builder().app(app).uniqueId(uniqueId).service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.CLOSED.getCode()).build();
        streamTaskMapper.updateStatus(task);

        return BaseResponseVo.ok();
    }

    @Override
    public Integer recoverForeverStreamTask() {
        StreamTaskDto streamTaskDto = StreamTaskDto.builder().service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).forever(1).lock(true).build();

        List<StreamTask> tasks = getStreamTask(streamTaskDto);

        if(tasks.isEmpty()) {
            return 0;
        }

        int success = 0;
        for (StreamTask task: tasks) {
            Boolean ok = recoverTask(task);
            if(ok) {
                success += 1;
            }
        }
        return success;
    }

    public Boolean recoverTask(StreamTask task) {
        if(serverConfig.getUseJavacv().equals("1")) {
            Thread process = streamTaskCache.getThread(task.getApp(), task.getUniqueId());
            if(process == null || !process.isAlive()) {
                javaCVHelper.asyncPullRtspPushRtmp(task.getOriginStream(), task.getApp(), task.getUniqueId());
                return true;
            }

        }else{
            Process process = streamTaskCache.getProcess(task.getApp(), task.getUniqueId());
            if(process == null || !process.isAlive()) {
                ffCommandHelper.transcodeStream(task.getOriginStream(), task.getApp(), task.getUniqueId());
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer closeDeadStreamTask() {
        StreamTaskDto streamTaskDto = StreamTaskDto.builder().service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).forever(0).lock(true).build();

        List<StreamTask> streamTasks = getStreamTask(streamTaskDto);
        int dead = 0;
        for(StreamTask task: streamTasks) {
            Boolean ok = closeTask(task);
            if(ok) {
                dead += 1;
            }
        }

        return dead;
    }

    public Boolean closeTask(StreamTask task) {
        if(serverConfig.getUseJavacv().equals("1")) {
            Thread thread = streamTaskCache.getThread(task.getApp(), task.getUniqueId());

            if(thread == null || !thread.isAlive()) {
                // 关闭
                StreamTask updateTask = StreamTask.builder().id(task.getId()).status(StreamTaskStatusEnum.CLOSED.getCode()).build();
                streamTaskMapper.updateStatus(updateTask);
                streamTaskCache.clearThread(task.getApp(), task.getUniqueId());
                return true;
            }

        }else{
            Process process = streamTaskCache.getProcess(task.getApp(), task.getUniqueId());

            if(process == null || !process.isAlive()) {
                // 关闭
                StreamTask updateTask = StreamTask.builder().id(task.getId()).status(StreamTaskStatusEnum.CLOSED.getCode()).build();
                streamTaskMapper.updateStatus(updateTask);
                streamTaskCache.clearProcess(task.getApp(), task.getUniqueId());
                return true;
            }
        }
        return false;
    }

    public  List<StreamTask> getStreamTask(StreamTaskDto streamTaskDto) {
        List<StreamTask> streamTasks = streamTaskMapper.selectByParam(streamTaskDto);
        if (streamTasks == null) {
            return new ArrayList<>();
        }

        if (streamTasks.size() == 0) {
            return new ArrayList<>();
        }

        return streamTasks;
    }
}
