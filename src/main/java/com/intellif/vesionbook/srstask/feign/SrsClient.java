package com.intellif.vesionbook.srstask.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "srs", url = "http://srs")
public class SrsClient {

}
