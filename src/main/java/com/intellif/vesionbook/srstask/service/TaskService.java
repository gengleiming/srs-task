package com.intellif.vesionbook.srstask.service;

import com.intellif.vesionbook.srstask.model.vo.rsp.StreamTaskRspVo;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.*;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;

import java.util.List;


public interface TaskService {

    BaseResponseVo<StreamTaskRspVo> getOrCreateStreamTask(TaskReqVo taskReqVo);

    BaseResponseVo<String> closeRtspStreamTask(CloseTaskReqVo closeTaskReqVo);

    BaseResponseVo<String> syncStreamTask(SyncReqVo syncReqVo);

    BaseResponseVo<List<StreamTaskRspVo>> aliveStreamTaskList(TaskListReqVo taskListReqVo);

    BaseResponseVo<StreamTaskRspVo> getGBStream(TaskReqVo taskReqVo);

    BaseResponseVo<GetStreamFromSrsRspVo.StreamData> getStreamInfo(StreamInfoReqVo streamInfoReqVo);

}
