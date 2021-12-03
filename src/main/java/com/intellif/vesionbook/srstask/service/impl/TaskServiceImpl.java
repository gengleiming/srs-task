package com.intellif.vesionbook.srstask.service.impl;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
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
import org.springframework.util.StringUtils;

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

    @Override
    @Transactional
    public BaseResponseVo<CreateTaskRspVo> createStreamTask(CreateTaskReqVo createTaskReqVo) {

        String app = createTaskReqVo.getApp();
        String uniqueId = createTaskReqVo.getUniqueId();
        String originStream = createTaskReqVo.getOriginStream();
        Integer outputType = createTaskReqVo.getOutputType();

        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId).origin(originStream).lock(true).build();
        List<StreamTask> streamTasks = streamTaskMapper.selectByParam(streamTaskDto);

        if(streamTasks!=null){
            streamTasks = streamTasks.stream().filter(item -> item.getStatus() != StreamTaskStatusEnum.CLOSED.getCode()).collect(Collectors.toList());
            if(streamTasks.size()>1) {
                return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_REPEAT);
            }
            if(streamTasks.size()==1) {
                StreamTask task = streamTasks.get(0);
                CreateTaskRspVo vo;
                if(outputType == StreamOutputTypeEnum.RTMP.getCode() && !StringUtils.isEmpty(task.getRtmpOutput())){
                    vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
                    return BaseResponseVo.ok(vo);
                }
                if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode() && !StringUtils.isEmpty(task.getHttpFlvOutput())){
                    vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
                    return BaseResponseVo.ok(vo);
                }
                if (outputType == StreamOutputTypeEnum.HLS.getCode() && !StringUtils.isEmpty(task.getHlsOutput())){
                    vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
                    return BaseResponseVo.ok(vo);
                }
                if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode() && !StringUtils.isEmpty(task.getWebrtcOutput())){
                    vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
                    return BaseResponseVo.ok(vo);
                }
            }
        }

        Process ffmpeg;
        if(outputType == StreamOutputTypeEnum.HTTP_HLV.getCode() || outputType == StreamOutputTypeEnum.WEB_RTC.getCode()) {
            ffmpeg = ffManagementHelper.transcodeToHlvAndRTC(originStream, app, uniqueId);
        } else {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }
        StreamTaskCache.taskMap.put(app + "-" + uniqueId, ffmpeg);
        return BaseResponseVo.ok();
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
