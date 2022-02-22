package com.intellif.vesionbook.srstask.controller;

import com.intellif.vesionbook.srstask.model.dto.VideoRecorderTaskDto;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.VideoRecorderTaskReqVo;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.intellif.vesionbook.srstask.service.VideoRecorderTaskService;
import com.intellif.vesionbook.srstask.model.entity.VideoRecorderTask;
import com.github.pagehelper.PageInfo;

@Slf4j
@Controller
@RequestMapping(value = "/api/video/recorder/task")
public class VideoRecorderTaskController
{
    @Autowired
    VideoRecorderTaskService videoRecorderTaskService;

    /**
    **  新增
    */
    @PostMapping(value = "/add")
    @ResponseBody
    public BaseResponseVo<String> add(@RequestBody VideoRecorderTaskReqVo reqVo) {
        log.info("create video recorder task reqVo: {}", reqVo);
        return videoRecorderTaskService.create(reqVo);
    }

    /**
    **  查询单个
    */
    @GetMapping(value = "/get")
    @ResponseBody
    public BaseResponseVo<VideoRecorderTask> selectById(String id) {
        VideoRecorderTask resp = videoRecorderTaskService.selectById(id);
        return BaseResponseVo.ok(resp);
    }

    /**
    **  查询列表
    */
    @GetMapping(value = "/list")
    @ResponseBody
    public BaseResponseVo<PageInfo<VideoRecorderTask>> selectList(VideoRecorderTaskDto videoRecorderTaskDto) {
        PageInfo<VideoRecorderTask> list = videoRecorderTaskService.getList(videoRecorderTaskDto);
        return BaseResponseVo.ok(list);
    }

}