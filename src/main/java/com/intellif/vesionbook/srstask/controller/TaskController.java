package com.intellif.vesionbook.srstask.controller;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.GetOrCreateTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@Api(tags = "任务管理器")
public class TaskController {

    @Resource
    private TaskService taskService;

    @Operation(summary = "创建流任务")
    @PostMapping("/get/or/create/stream/task")
    public BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(@RequestBody GetOrCreateTaskReqVo getOrCreateTaskReqVo) {
        log.info("cache: {}", StreamTaskCache.taskMap);
        String originUrl = "rtsp://admin:intellif123@192.168.18.5/live/livestream";
        getOrCreateTaskReqVo.setOriginStream(originUrl);
        return taskService.getOrCreateStreamTask(getOrCreateTaskReqVo);
    }

    @Operation(summary = "关闭流任务")
    @PostMapping("/close/stream/task")
    public BaseResponseVo<String> closeStreamTask(@RequestBody CloseTaskReqVo closeTaskReqVo) {
        log.info("cache: {}", StreamTaskCache.taskMap);
        String originUrl = "rtsp://admin:intellif123@192.168.18.5/live/livestream";
        closeTaskReqVo.setOriginStream(originUrl);
        return taskService.closeStreamTask(closeTaskReqVo);
    }
}
