package com.intellif.vesionbook.srstask.scheduled;

import com.intellif.vesionbook.srstask.helper.SrsClientHelper;
import com.intellif.vesionbook.srstask.model.dto.CacheDeadDto;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetGBDataFromSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScheduledTask {

    private final Map<String, CacheDeadDto> deadMap =  new HashMap<>();
    @Resource
    private TaskService taskService;
    @Resource
    private SrsClientHelper srsClientHelper;

    /**
     * 启动服务3分钟之后，1分钟检测一次，检测流任务的客户端数量
     */
    @Scheduled(initialDelay = 180000, fixedDelay = 60000)
    public void checkStreamAndCloseUnused() {
        log.info("dead map: {}", deadMap);
        List<GetStreamFromSrsRspVo.StreamData> streamsWithNoClients = srsClientHelper.getStreamsWithNoClients();
        if(streamsWithNoClients == null) {
            return;
        }
        List<String> streamIdList = streamsWithNoClients.stream().map(GetStreamFromSrsRspVo.StreamData::getId).collect(Collectors.toList());
        Set<String> keys = deadMap.keySet();
        List<String> removeList = keys.stream().filter(item->!streamIdList.contains(item)).collect(Collectors.toList());

        for (String key : removeList) {
            deadMap.remove(key);
        }

        for (GetStreamFromSrsRspVo.StreamData stream : streamsWithNoClients) {
            CacheDeadDto cachedeaddto = new CacheDeadDto();
            cachedeaddto.setApp(stream.getApp());
            cachedeaddto.setName(stream.getName());

            if(deadMap.containsKey(stream.getId())) {
                cachedeaddto = deadMap.get(stream.getId());
                cachedeaddto.setCount(cachedeaddto.getCount()+1);
            }else{
                cachedeaddto.setCount(1);
            }
            deadMap.put(stream.getId(), cachedeaddto);
        }

        // 先清除gb28181
        closeUnusedGB(deadMap);
        // 再清除剩余的，剩余的目前只有rtsp
        closeUnusedRtsp(deadMap);
    }

    public void closeUnusedRtsp(Map<String, CacheDeadDto> deadMap) {
        log.info("scheduled 检测关闭 dead Rtsp...");

        List<String> removeList = new ArrayList<>();
        for(Map.Entry<String, CacheDeadDto> entry: deadMap.entrySet()) {
            String key = entry.getKey();
            CacheDeadDto cacheDeadDto = entry.getValue();
            if(cacheDeadDto.getCount() <= 3) {
                continue;
            }

            CloseTaskReqVo reqVo = CloseTaskReqVo.builder().app(cacheDeadDto.getApp()).uniqueId(cacheDeadDto.getName()).build();
            log.info("scheduled关闭流. stream: {}", cacheDeadDto);
            taskService.closeRtspStreamTask(reqVo);

            removeList.add(key);
        }

        for(String key: removeList) {
            deadMap.remove(key);
        }
    }

    public void closeUnusedGB(Map<String, CacheDeadDto> deadMap) {
        log.info("scheduled 检测关闭 dead gb28181...");

        List<GetGBDataFromSrsRspVo.ChannelData> gbChannels = srsClientHelper.getGBChannels();

        List<String> removeList = new ArrayList<>();
        for (Map.Entry<String, CacheDeadDto> entry : deadMap.entrySet()) {
            String key = entry.getKey();
            CacheDeadDto cacheDeadDto = entry.getValue();
            if (cacheDeadDto.getCount() <= 3) {
                continue;
            }

            CacheDeadDto value = entry.getValue();
            long count = gbChannels.stream().filter(item -> item.getApp().equals(value.getApp()) && item.getStream().equals(value.getName())).count();
            if(count > 0) {
                String id = value.getName().split("@")[0];
                String chid = value.getName().split("@")[1];
                srsClientHelper.closeChannel(id, chid);
                removeList.add(key);
            }
        }

        for(String key: removeList) {
            deadMap.remove(key);
        }
    }
}
