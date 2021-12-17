package com.intellif.vesionbook.srstask.feign;

import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "srs", url = "${srs.api.url:http://192.168.18.151:1985}")
public interface SrsClient {
    @GetMapping(value = "/api/v1/streams/")
    GetStreamFromSrsRspVo getStreams();
}
