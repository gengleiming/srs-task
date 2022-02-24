package com.intellif.vesionbook.srstask.mapper;

import com.intellif.vesionbook.srstask.model.dto.UpdateStatusVideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.dto.VideoRecorderTaskDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import com.intellif.vesionbook.srstask.model.entity.VideoRecorderTask;

@Mapper
public interface VideoRecorderTaskMapper {

    void insertSelective(List<VideoRecorderTask> videoRecorderTaskList);

    List<VideoRecorderTask> selectByParam(VideoRecorderTaskDto videoRecorderTaskDto);

    VideoRecorderTask selectById(String id);

    void updateStatus(UpdateStatusVideoRecorderTaskDto updateStatusVideoRecorderTaskDto);

}