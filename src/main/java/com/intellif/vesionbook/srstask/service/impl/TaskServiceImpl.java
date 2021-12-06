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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

        BaseResponseVo<CreateTaskRspVo> outputStream = getStreamByOutputType(app, uniqueId, outputType);
        if(outputStream != null) {
            return outputStream;
        }

        Process ffmpeg;
        if(outputType == StreamOutputTypeEnum.HTTP_HLV.getCode() || outputType == StreamOutputTypeEnum.WEB_RTC.getCode()
                || outputType == StreamOutputTypeEnum.RTMP.getCode()) {
            ffmpeg = ffManagementHelper.transcodeToRtmpAndHlvAndRTC(originStream, app, uniqueId);
            // 缓存process
            StreamTaskCache.storeProcess(app, uniqueId, ffmpeg);

            Integer outputTypes = StreamOutputTypeEnum.RTMP.getCode() | StreamOutputTypeEnum.HTTP_HLV.getCode() | StreamOutputTypeEnum.WEB_RTC.getCode();
            StreamTask streamTask = new StreamTask();
            streamTask.setApp(app);
            streamTask.setUniqueId(uniqueId);
            streamTask.setOriginStream(originStream);
            streamTask.setOutputTypes(outputTypes);
            streamTask.setService(serverConfig.getServiceHost());

            streamTask.setService(serverConfig.getServiceHost());


        } else {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }
        StreamTaskCache.taskMap.put(app + "-" + uniqueId, ffmpeg);
        return BaseResponseVo.ok();
    }

    public BaseResponseVo<CreateTaskRspVo> getStreamByOutputType(String app, String uniqueId, Integer outputType) {
        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId).lock(true).build();
        List<StreamTask> streamTasks = streamTaskMapper.selectByParam(streamTaskDto);

        if(streamTasks==null){
            return null;
        }

        streamTasks = streamTasks.stream().filter(
                item -> item.getStatus() != StreamTaskStatusEnum.CLOSED.getCode() &&
                (item.getOutputTypes()&outputType)==outputType).collect(Collectors.toList());

        if(streamTasks.size()==0){
            return null;
        }

        if(streamTasks.size()>1) {
            log.error("流任务重复: {}", streamTasks);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_REPEAT);
        }

        StreamTask task = streamTasks.get(0);
        CreateTaskRspVo vo = CreateTaskRspVo.builder().outputType(outputType).outputStream(task.getOutputStream()).build();
        return BaseResponseVo.ok(vo);
    }

    @Override
    public void destroyStreamTask(DestroyTaskReqVo destroyTaskReqVo) {
        String app = destroyTaskReqVo.getApp();
        String uniqueId = destroyTaskReqVo.getUniqueId();
        String originStream = destroyTaskReqVo.getOriginStream();
        Process process = StreamTaskCache.taskMap.get(app + "-" + uniqueId);
        process.destroy();
    }
}
