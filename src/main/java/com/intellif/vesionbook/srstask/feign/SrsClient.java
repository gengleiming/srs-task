package com.intellif.vesionbook.srstask.feign;

import com.intellif.vesionbook.srstask.model.vo.rsp.BaseSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetClientsFromSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetGBDataFromSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "srs", url = "${srs.api.url}")
public interface SrsClient {
    @GetMapping(value = "/api/v1/streams")
    GetStreamFromSrsRspVo getStreams();

    @DeleteMapping(value = "/api/v1/clients/{clientId}")
    BaseSrsRspVo kickOffClient(@PathVariable("clientId") String clientId);

    @GetMapping(value = "/api/v1/clients")
    GetClientsFromSrsRspVo getClients(@RequestParam("start") String start, @RequestParam("count") String count);

    @GetMapping(value = "/api/v1/gb28181")
    GetGBDataFromSrsRspVo getGBData(@RequestParam("action") String action, @RequestParam("id") String id,
                                    @RequestParam("chid") String chid);
}
