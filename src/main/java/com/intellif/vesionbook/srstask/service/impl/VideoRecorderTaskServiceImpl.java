package com.intellif.vesionbook.srstask.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.enums.StreamOutputTypeEnum;
import com.intellif.vesionbook.srstask.enums.VideoRecorderTaskStatusEnum;
import com.intellif.vesionbook.srstask.mapper.VideoRecorderTaskMapper;
import com.intellif.vesionbook.srstask.model.dto.VideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.entity.VideoRecorderTask;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.TimeRange;
import com.intellif.vesionbook.srstask.model.vo.req.VideoRecorderTaskReqVo;
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
    VideoRecorderTaskMapper videoRecorderTaskMapper;

    @Override
    public BaseResponseVo<String> create(VideoRecorderTaskReqVo reqVo) {
        TaskReqVo taskReqVo = TaskReqVo.builder().app(reqVo.getApp()).uniqueId(reqVo.getUniqueId())
                .originStream(reqVo.getOriginStream()).channelId(reqVo.getChannelId())
                .outputType(StreamOutputTypeEnum.RTMP.getCode()).build();

        if (StringUtils.isEmpty(reqVo.getChannelId()) && StringUtils.isEmpty(reqVo.getOriginStream())) {
            return BaseResponseVo.error(ReturnCodeEnum.PARAM_INVALID);
        }

        if (!StringUtils.isEmpty(reqVo.getChannelId()) && !StringUtils.isEmpty(reqVo.getOriginStream())) {
            return BaseResponseVo.error(ReturnCodeEnum.PARAM_INVALID);
        }

        String uniqueId;
        // gb28181
        if (!StringUtils.isEmpty(reqVo.getChannelId())) {
//            streamResult = taskService.getGBStream(taskReqVo);
            uniqueId = reqVo.getUniqueId() + "@" + reqVo.getChannelId();
        } else {
//            streamResult = taskService.getOrCreateStreamTask(taskReqVo);
            uniqueId = reqVo.getUniqueId();
        }

//        if(!streamResult.isSuccess()) {
//            log.error("video recorder. get rtsp stream result error: {}", streamResult);
//            return BaseResponseVo.error(ReturnCodeEnum.ERROR_GET_STREAM_TASK_ERROR);
//        }

        List<VideoRecorderTask> videoRecorderTasks = new ArrayList<>();
        for (TimeRange timeRange : reqVo.getTimeRangeList()) {
            VideoRecorderTask model = VideoRecorderTask.builder().app(reqVo.getApp()).uniqueId(uniqueId)
                    .status(VideoRecorderTaskStatusEnum.INIT.getCode()).startTime(timeRange.getStartTime())
                    .endTime(timeRange.getEndTime()).build();
            videoRecorderTasks.add(model);
        }

        videoRecorderTaskMapper.insertSelective(videoRecorderTasks);

//        String rtmpOutput = streamResult.getData().getRtmpOutput();
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

}
