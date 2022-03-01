package com.intellif.vesionbook.srstask.controller;

import com.intellif.vesionbook.srstask.model.dto.VideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.VideoRecorderTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.VideoRecorderTaskListVo;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.intellif.vesionbook.srstask.service.VideoRecorderTaskService;
import com.intellif.vesionbook.srstask.model.entity.VideoRecorderTask;
import com.github.pagehelper.PageInfo;

@Slf4j
@RestController
@RequestMapping(value = "/api/video/recorder/task")
public class VideoRecorderTaskController
{
    @Autowired
    VideoRecorderTaskService videoRecorderTaskService;

    /**
    **  新增
    */
    @PostMapping(value = "/add")
    public BaseResponseVo<String> add(@RequestBody @Validated VideoRecorderTaskReqVo reqVo) {
        log.info("create video recorder task reqVo: {}", reqVo);
        return videoRecorderTaskService.create(reqVo);
    }

    /**
    **  查询单个
    */
    @PostMapping(value = "/get/{id}")
    public BaseResponseVo<VideoRecorderTask> selectById(@PathVariable("id") Long id) {
        VideoRecorderTask resp = videoRecorderTaskService.selectById(id);
        return BaseResponseVo.ok(resp);
    }

    /**
    **  查询列表
    */
    @PostMapping(value = "/list")
    public BaseResponseVo<PageInfo<VideoRecorderTaskListVo>> selectList(@RequestBody VideoRecorderTaskDto videoRecorderTaskDto) {
        log.info("videoRecorderTaskDto: {}", videoRecorderTaskDto);
        PageInfo<VideoRecorderTaskListVo> list = videoRecorderTaskService.getList(videoRecorderTaskDto, true);
        return BaseResponseVo.ok(list);
    }

}