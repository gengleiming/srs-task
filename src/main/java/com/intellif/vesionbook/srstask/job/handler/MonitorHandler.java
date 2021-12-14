package com.intellif.vesionbook.srstask.job.handler;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.service.TaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.handler.annotation.RegisterJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RegisterJobHandler(name = "MonitorHandler", cron = "0 */1 * * * ?", desc="流任务调度检测", author = "gengleiming")
@JobHandler(value = "MonitorHandler")
public class MonitorHandler extends IJobHandler {

    @Resource
    StreamTaskCache streamTaskCache;
    @Resource
    TaskService taskService;

    @Override
    public ReturnT<String> execute(String s) {
        log.info("monitor job task.");
        try {
            XxlJobLogger.log("stream cache process: {}, thread: {}", streamTaskCache.getProcessMap(), streamTaskCache.getThreadMap());
            Integer recover = taskService.recoverForeverStreamTask();
            XxlJobLogger.log("monitor recover success: {}", recover);
            Integer dead = taskService.closeDeadStreamTask();
            XxlJobLogger.log("monitor close dead: {}", dead);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }
}
