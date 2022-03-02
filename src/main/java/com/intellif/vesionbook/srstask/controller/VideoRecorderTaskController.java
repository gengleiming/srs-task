package com.intellif.vesionbook.srstask.controller;

import com.github.pagehelper.PageInfo;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.GetOssUrlReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.GetOssUrlRspVo;
import com.intellif.vesionbook.srstask.model.vo.req.VideoRecorderReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.VideoRecorderTaskListVo;
import com.intellif.vesionbook.srstask.service.VideoRecorderTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping(value = "/api/video/recorder/task")
public class VideoRecorderTaskController {
    @Resource
    VideoRecorderTaskService videoRecorderTaskService;

    /**
     * 开启录像
     */
    @PostMapping(value = "/start")
    public BaseResponseVo<PageInfo<VideoRecorderTaskListVo>> start(@RequestBody VideoRecorderReqVo videoRecorderReqVo) {
        log.info("videoRecorderStartReqVo: {}", videoRecorderReqVo);
        videoRecorderTaskService.start(videoRecorderReqVo);
        return BaseResponseVo.ok();
    }

    /**
     * 停止录像
     */
    @PostMapping(value = "/stop")
    public BaseResponseVo<String> stop(@RequestBody VideoRecorderReqVo videoRecorderReqVo) {
        log.info("videoRecorderStopReqVo: {}", videoRecorderReqVo);
        return videoRecorderTaskService.stop(videoRecorderReqVo);
    }

    /**
     * 获取oss下载地址
     */
    @PostMapping(value = "/get/oss/url")
    public BaseResponseVo<GetOssUrlRspVo> getOssUrl(@RequestBody GetOssUrlReqVo getOssUrlReqVo) {
        log.info("getOssUrlReqVo: {}", getOssUrlReqVo);
        return videoRecorderTaskService.getOssUrl(getOssUrlReqVo);
    }
}