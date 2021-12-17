package com.intellif.vesionbook.srstask.service;

import com.intellif.vesionbook.srstask.model.entity.StreamTask;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskListReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.SyncReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;

import java.util.List;


public interface TaskService {

    BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(TaskReqVo taskReqVo);
    BaseResponseVo<String> closeStreamTask(CloseTaskReqVo closeTaskReqVo);

    BaseResponseVo<String> syncStreamTask(SyncReqVo syncReqVo);

    Integer recoverForeverStreamTask();

    Integer cleanOldTaskWhileStart();

    BaseResponseVo<List<StreamTask>> aliveStreamTaskList(TaskListReqVo taskListReqVo);

}
