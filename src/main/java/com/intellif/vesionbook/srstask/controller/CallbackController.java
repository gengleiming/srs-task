package com.intellif.vesionbook.srstask.controller;

import com.alibaba.fastjson.JSONObject;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.helper.SrsClientHelper;
import com.intellif.vesionbook.srstask.model.vo.req.SRSCallbackOnDvrVo;
import com.intellif.vesionbook.srstask.model.vo.req.SRSCallbackOnPlayVo;
import com.intellif.vesionbook.srstask.service.VideoRecorderTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.FileNotFoundException;

@RestController
@Slf4j
@Api(tags = "回调接口")
@RequestMapping("/api/callback/")
public class CallbackController {

    @Resource
    SrsClientHelper srsClientHelper;
    @Resource
    ServerConfig serverConfig;
    @Resource
    VideoRecorderTaskService videoRecorderTaskService;

    @ApiOperation(value = "srs播放回调接口")
    @PostMapping("/on/play")
    public int onPlay(@RequestBody @Validated SRSCallbackOnPlayVo reqVo) {
        log.info("req: {}", reqVo);
        Integer clientsNum = srsClientHelper.getClientsNum();
        if(clientsNum >= serverConfig.getClientsLimit()){
            log.error("on play. client num limit, client num: {}", clientsNum);
            return -1;
        }
        return 0;
    }

    @ApiOperation(value = "srs录像回调接口")
    @PostMapping("/on/dvr")
    public int onDvr(@RequestBody @Validated SRSCallbackOnDvrVo reqVo) throws FileNotFoundException {
        log.info("req: {}", reqVo);
        videoRecorderTaskService.dealOnDvr(reqVo);
        return 0;
    }
}
