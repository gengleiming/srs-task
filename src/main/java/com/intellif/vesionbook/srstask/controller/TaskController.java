package com.intellif.vesionbook.srstask.controller;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.GetOrCreateTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@Api(tags = "任务管理器")
public class TaskController {

    @Resource
    private TaskService taskService;

    @Resource
    private StreamTaskCache streamTaskCache;

    @ApiOperation(value = "创建流任务")
    @PostMapping("/get/or/create/stream/task")
    public BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(@RequestBody @Validated GetOrCreateTaskReqVo getOrCreateTaskReqVo) {
        log.info("process number: {}, thread number: {}", streamTaskCache.getProcessNumber(), streamTaskCache.getThreadNumber());
        return taskService.getOrCreateStreamTask(getOrCreateTaskReqVo);
    }

    @ApiOperation(value = "关闭流任务")
    @PostMapping("/close/stream/task")
    public BaseResponseVo<String> closeStreamTask(@RequestBody @Validated CloseTaskReqVo closeTaskReqVo) {
        log.info("process number: {}, thread number: {}", streamTaskCache.getProcessNumber(), streamTaskCache.getThreadNumber());
        return taskService.closeStreamTask(closeTaskReqVo);
    }
}
