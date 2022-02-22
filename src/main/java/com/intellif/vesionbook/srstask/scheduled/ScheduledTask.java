package com.intellif.vesionbook.srstask.scheduled;

import com.intellif.vesionbook.srstask.cache.StreamDeadMapCache;
import com.intellif.vesionbook.srstask.helper.SrsClientHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@EnableAsync
public class ScheduledTask {

    @Resource
    private StreamDeadMapCache streamDeadMapCache;
    @Resource
    private SrsClientHelper srsClientHelper;

    /**
     * 启动服务3分钟之后，10秒钟检测一次清除无用连接
     */
    @Async
    @Scheduled(initialDelay = 180000, fixedDelay = 10000)
    public void checkStreamAndCloseUnused() {
        // 首先检测关闭异常通道
        srsClientHelper.closeErrorGBChannels();
        // 构建并更新deadMap
        streamDeadMapCache.updateDeadMapCache();
        // 先清除gb28181
        streamDeadMapCache.clearUnusedGB();
        // 再清除rtsp
        streamDeadMapCache.closeUnusedRtsp();
    }

    /**
     * 每5秒钟检测录像任务
     */
    @Async
    @Scheduled(fixedDelay = 5000)
    public void checkVideoRecorder() {
        // 首先检测关闭异常通道
        srsClientHelper.closeErrorGBChannels();
        // 构建并更新deadMap
        streamDeadMapCache.updateDeadMapCache();
        // 先清除gb28181
        streamDeadMapCache.clearUnusedGB();
        // 再清除rtsp
        streamDeadMapCache.closeUnusedRtsp();
    }

}
