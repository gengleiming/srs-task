package com.intellif.vesionbook.srstask.controller;

import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.CreateTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.DestroyTaskReqVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Api(tags = "任务管理器")
public class TaskController {

    @Resource
    private TaskService taskService;

    @Operation(summary = "创建流任务")
    @PostMapping("/create/stream/task")
    public BaseResponseVo<String> createStreamTask(@RequestBody CreateTaskReqVo createTaskReqVo) {
        String originUrl = "rtsp://admin:intellif123@192.168.18.5/live/livestream";
        createTaskReqVo.setOriginStream(originUrl);
        taskService.createStreamTask(createTaskReqVo);
        return BaseResponseVo.ok();
    }

    @Operation(summary = "关闭流任务")
    @PostMapping("/destroy/stream/task")
    public BaseResponseVo<String> destroyStreamTask(@RequestBody DestroyTaskReqVo destroyTaskReqVo) {
        String originUrl = "rtsp://admin:intellif123@192.168.18.5/live/livestream";
        destroyTaskReqVo.setOriginStream(originUrl);
        taskService.destroyStreamTask(destroyTaskReqVo);
        return BaseResponseVo.ok();
    }
}
