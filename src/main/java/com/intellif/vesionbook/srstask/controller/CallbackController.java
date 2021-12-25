package com.intellif.vesionbook.srstask.controller;

import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.helper.SrsClientHelper;
import com.intellif.vesionbook.srstask.model.vo.req.SRSCallbackVo;
import com.mysql.fabric.Server;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@Api(tags = "回调接口")
public class CallbackController {

    @Resource
    SrsClientHelper srsClientHelper;
    @Resource
    ServerConfig serverConfig;

    @ApiOperation(value = "播放回调接口")
    @PostMapping("/callback/on/play")
    public int getOrCreateStreamTask(@RequestBody @Validated SRSCallbackVo reqVo) {
        log.info("req: {}", reqVo);
        Integer clientsNum = srsClientHelper.getClientsNum();
        if(clientsNum >= Integer.parseInt(serverConfig.getClientsLimit())){
            log.error("on play. client num limit, client num: {}", clientsNum);
            return -1;
        }
        return 0;
    }
}
