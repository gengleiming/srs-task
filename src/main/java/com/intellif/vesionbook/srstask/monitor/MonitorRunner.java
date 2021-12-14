package com.intellif.vesionbook.srstask.monitor;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class MonitorRunner implements ApplicationRunner {

    @Resource
    private TaskService taskService;

    @Resource
    private StreamTaskCache streamTaskCache;

    @Override
    public void run(ApplicationArguments var) {
        try {
            log.info("stream cache process: {}, thread: {}", streamTaskCache.getProcessMap(), streamTaskCache.getThreadMap());
            Integer recover = taskService.recoverForeverStreamTask();
            log.info("monitor recover success: {}", recover);
            Integer dead = taskService.closeDeadStreamTask();
            log.info("monitor close dead: {}", dead);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
