package com.intellif.vesionbook.srstask.service;

import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.GetOssUrlReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.GetOssUrlRspVo;
import com.intellif.vesionbook.srstask.model.vo.req.SRSCallbackOnDvrVo;
import com.intellif.vesionbook.srstask.model.vo.req.VideoRecorderReqVo;

import java.io.FileNotFoundException;

public interface VideoRecorderTaskService {
    void dealOnDvr(SRSCallbackOnDvrVo vo) throws FileNotFoundException;

    void start(VideoRecorderReqVo vo);

    BaseResponseVo<String> stop(VideoRecorderReqVo vo);

    BaseResponseVo<GetOssUrlRspVo> getOssUrl(GetOssUrlReqVo vo);
}
