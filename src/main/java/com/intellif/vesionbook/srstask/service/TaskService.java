package com.intellif.vesionbook.srstask.service;

import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.CreateTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.DestroyTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;


public interface TaskService {

    BaseResponseVo<CreateTaskRspVo> createStreamTask(CreateTaskReqVo createTaskReqVo);
    BaseResponseVo<String> deleteStreamTask(DestroyTaskReqVo destroyTaskReqVo);

}
