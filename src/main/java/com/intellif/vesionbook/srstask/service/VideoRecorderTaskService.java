package com.intellif.vesionbook.srstask.service;

import com.github.pagehelper.PageInfo;
import com.intellif.vesionbook.srstask.model.dto.VideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.entity.VideoRecorderTask;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.VideoRecorderTaskReqVo;

public interface VideoRecorderTaskService
{
    BaseResponseVo<String> create(VideoRecorderTaskReqVo model);

    VideoRecorderTask selectById(String id);

    PageInfo<VideoRecorderTask> getList(VideoRecorderTaskDto videoRecorderTaskDto);

    void videoRecordStart();
    void videoRecordStop();

}
