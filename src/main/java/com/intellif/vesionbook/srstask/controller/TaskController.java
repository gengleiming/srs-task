package com.intellif.vesionbook.srstask.controller;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.model.entity.StreamTask;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskListReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.SyncReqVo;
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
import java.util.List;

@RestController
@Slf4j
@Api(tags = "任务管理器")
public class TaskController {

    @Resource
    private TaskService taskService;

    @ApiOperation(value = "创建流任务")
    @PostMapping("/get/or/create/stream/task")
    public BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(@RequestBody @Validated TaskReqVo taskReqVo) {
        log.info("req: {}", taskReqVo);
        return taskService.getOrCreateStreamTask(taskReqVo);
    }

    @ApiOperation(value = "关闭流任务")
    @PostMapping("/close/stream/task")
    public BaseResponseVo<String> closeStreamTask(@RequestBody @Validated CloseTaskReqVo closeTaskReqVo) {
        log.info("req: {}", closeTaskReqVo);
        return taskService.closeStreamTask(closeTaskReqVo);
    }

    @ApiOperation(value = "同步流任务")
    @PostMapping("/sync/stream/task")
    public BaseResponseVo<String> syncStreamTask(@RequestBody @Validated SyncReqVo syncReqVo) {
        log.info("req: {}", syncReqVo);
        return taskService.syncStreamTask(syncReqVo);
    }

    @ApiOperation(value = "获取正在运行流任务列表")
    @PostMapping("/stream/task/alive/list")
    public BaseResponseVo<List<StreamTask>> streamTaskList(@RequestBody @Validated TaskListReqVo taskListReqVo) {
        log.info("req: {}", taskListReqVo);
        return taskService.aliveStreamTaskList(taskListReqVo);
    }
}
