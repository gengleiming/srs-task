package com.intellif.vesionbook.srstask.service;

import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.GetOrCreateTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;


public interface TaskService {

    BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(GetOrCreateTaskReqVo getOrCreateTaskReqVo);
    BaseResponseVo<String> closeStreamTask(CloseTaskReqVo closeTaskReqVo);

}
