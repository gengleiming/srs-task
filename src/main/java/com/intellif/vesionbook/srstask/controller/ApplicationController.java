/*
 * 文件名：HeartController
 * 版权：Copyright by 云天励飞 intellif.com
 * 描述：
 * 创建人：yuzhilong
 * 创建时间：2020/8/13 20:12
 * 修改理由：
 * 修改内容：
 */
package com.intellif.vesionbook.srstask.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;

@Api(tags = "应用")
@RestController
@Slf4j
public class ApplicationController {

    @ApiOperation(value = "连通性测试")
    @GetMapping("/")
    public String index(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        return "<h1>Hello, SRS Task!</h1><br/> server start at : " + format.format(new Date(bean.getStartTime()));
    }
}