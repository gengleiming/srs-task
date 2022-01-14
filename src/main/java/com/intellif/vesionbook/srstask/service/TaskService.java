package com.intellif.vesionbook.srstask.service;

import com.intellif.vesionbook.srstask.model.entity.StreamTask;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.*;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;

import java.util.List;


public interface TaskService {

    BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(TaskReqVo taskReqVo);
    BaseResponseVo<String> closeRtspStreamTask(CloseTaskReqVo closeTaskReqVo);

    BaseResponseVo<String> syncStreamTask(SyncReqVo syncReqVo);

    Integer recoverForeverStreamTask();

    Integer cleanOldTaskWhileStart();

    BaseResponseVo<List<StreamTask>> aliveStreamTaskList(TaskListReqVo taskListReqVo);

    BaseResponseVo<CreateTaskRspVo> getGBStream(TaskReqVo taskReqVo);

    BaseResponseVo<GetStreamFromSrsRspVo.StreamData> getStreamInfo(StreamInfoReqVo streamInfoReqVo);

}
