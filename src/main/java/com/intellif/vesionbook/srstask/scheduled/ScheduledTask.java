package com.intellif.vesionbook.srstask.scheduled;

import com.intellif.vesionbook.srstask.feign.SrsClient;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ScheduledTask {

    private final Map<String, Integer> deadCountMap =  new HashMap<>();
    @Resource
    private SrsClient srsClient;
    @Resource
    private TaskService taskService;

    /**
     * 启动服务10分钟之后，一分钟检测一次，检测流任务的客户端数量
     */
    @Scheduled(initialDelay = 600000, fixedDelay = 60000)
    public void checkStreamAndCloseUnused() {
        log.info("scheduled 开始检测流任务客户端连接数...");
        GetStreamFromSrsRspVo response = srsClient.getStreams();
        if(response.getCode() != 0) {
            log.error("srs api server return error. response: {}", response);
            return;
        }
        List<GetStreamFromSrsRspVo.StreamData> streams = response.getStreams();
        log.info("scheduled all streams: {}", streams);
        for(GetStreamFromSrsRspVo.StreamData stream: streams) {
            String key = stream.getApp() + "-" + stream.getName();

            if(!stream.getPublish().getActive() || stream.getClients() > 1) {
                deadCountMap.remove(key);
                continue;
            }

            if(!deadCountMap.containsKey(key)){
                deadCountMap.put(key, 1);
                continue;
            }

            if(deadCountMap.get(key) <= 3) {
                deadCountMap.put(key, deadCountMap.get(key) + 1);
                continue;
            }

            CloseTaskReqVo reqVo = CloseTaskReqVo.builder().app(stream.getApp()).uniqueId(stream.getName()).build();
            log.info("scheduled关闭流. stream: {}", stream);
            taskService.closeStreamTask(reqVo);

            deadCountMap.remove(key);
        }

    }
}
