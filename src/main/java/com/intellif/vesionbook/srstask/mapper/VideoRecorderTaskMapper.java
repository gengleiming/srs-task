package com.intellif.vesionbook.srstask.mapper;

import com.intellif.vesionbook.srstask.model.dto.UpdateVideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.dto.VideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.vo.rsp.VideoRecorderTaskListVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import com.intellif.vesionbook.srstask.model.entity.VideoRecorderTask;

@Mapper
public interface VideoRecorderTaskMapper {

    void insertSelective(VideoRecorderTask videoRecorderTask);

    void insertBatch(List<VideoRecorderTask> list);

    List<VideoRecorderTaskListVo> selectByParam(VideoRecorderTaskDto videoRecorderTaskDto);

    VideoRecorderTask selectById(String id);

    void updateStatus(UpdateVideoRecorderTaskDto updateVideoRecorderTaskDto);

    void updatePathById(UpdateVideoRecorderTaskDto updateVideoRecorderTaskDto);

}