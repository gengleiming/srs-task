package com.intellif.vesionbook.srstask.cache;

import com.intellif.vesionbook.srstask.helper.SrsClientHelper;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetGBDataFromSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Data
class CacheDead {
    private String app; // app
    private String name; // stream
    private Integer count; // 计数
}

@Component
@Slf4j
public class StreamDeadMapCache {
    private final Map<String, CacheDead> deadMap = new HashMap<>();

    @Resource
    SrsClientHelper srsClientHelper;
    @Resource
    TaskService taskService;

    public void removeAliveStream(List<String> streamIdList) {
        Set<String> keys = deadMap.keySet();
        List<String> removeList = keys.stream().filter(item -> !streamIdList.contains(item)).collect(Collectors.toList());
        for (String key : removeList) {
            deadMap.remove(key);
        }
    }

    public void updateDeadMap(GetStreamFromSrsRspVo.StreamData streamMarkDead) {
        CacheDead cachedeaddto = new CacheDead();
        cachedeaddto.setApp(streamMarkDead.getApp());
        cachedeaddto.setName(streamMarkDead.getName());

        if (deadMap.containsKey(streamMarkDead.getId())) {
            cachedeaddto = deadMap.get(streamMarkDead.getId());
            cachedeaddto.setCount(cachedeaddto.getCount() + 1);
        } else {
            cachedeaddto.setCount(1);
        }
        deadMap.put(streamMarkDead.getId(), cachedeaddto);
    }

    public void updateDeadMapCache() {
        List<GetStreamFromSrsRspVo.StreamData> streamsWithNoClients = srsClientHelper.getStreamsWithNoClients();
        if (streamsWithNoClients == null) {
            return;
        }
        List<String> streamIdList = streamsWithNoClients.stream().map(GetStreamFromSrsRspVo.StreamData::getId).collect(Collectors.toList());
        removeAliveStream(streamIdList);

        for (GetStreamFromSrsRspVo.StreamData stream : streamsWithNoClients) {
            updateDeadMap(stream);
        }
    }


    public void clearUnusedGB() {
        List<GetGBDataFromSrsRspVo.ChannelData> gbChannels = srsClientHelper.getGBChannels();

        List<String> removeList = new ArrayList<>();
        for (Map.Entry<String, CacheDead> entry : deadMap.entrySet()) {
            String key = entry.getKey();
            CacheDead cacheDead = entry.getValue();
            if (cacheDead.getCount() <= 3) {
                continue;
            }

            CacheDead value = entry.getValue();
            long count = gbChannels.stream().filter(item -> item.getApp().equals(value.getApp()) && item.getStream().equals(value.getName())).count();
            if (count > 0) {
                log.info("检测关闭 dead gb28181 stream id: {}", value.getName());
                String id = value.getName().split("@")[0];
                String chid = value.getName().split("@")[1];
                srsClientHelper.closeChannel(id, chid);
                removeList.add(key);
            }
        }

        for (String key : removeList) {
            deadMap.remove(key);
        }
    }

    public void closeUnusedRtsp() {
        List<String> removeList = new ArrayList<>();
        for (Map.Entry<String, CacheDead> entry : deadMap.entrySet()) {
            String key = entry.getKey();
            CacheDead cacheDead = entry.getValue();
            if (cacheDead.getCount() <= 3) {
                continue;
            }

            CloseTaskReqVo reqVo = CloseTaskReqVo.builder().app(cacheDead.getApp()).uniqueId(cacheDead.getName()).build();
            taskService.closeRtspStreamTask(reqVo);

            removeList.add(key);
        }

        for (String key : removeList) {
            deadMap.remove(key);
        }
    }

}
