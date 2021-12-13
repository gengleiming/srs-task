package com.intellif.vesionbook.srstask.service;

import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.SyncTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;


public interface TaskService {

    BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(TaskReqVo taskReqVo);
    BaseResponseVo<String> closeStreamTask(CloseTaskReqVo closeTaskReqVo);

    BaseResponseVo<String> syncStreamTask(SyncTaskReqVo syncTaskReqVo);

    Integer recoverForeverStreamTask();

    Integer closeDeadStreamTask();

}
