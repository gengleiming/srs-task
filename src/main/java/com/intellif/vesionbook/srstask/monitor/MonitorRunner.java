package com.intellif.vesionbook.srstask.monitor;

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

    @Override
    public void run(ApplicationArguments var) {
        do {
            try {
                Integer recover = taskService.recoverForeverStreamTask();
                log.info("monitor recover success: {}", recover);
                Integer dead = taskService.closeDeadStreamTask();
                log.info("monitor close dead: {}", dead);
                Thread.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (true);
    }
}
