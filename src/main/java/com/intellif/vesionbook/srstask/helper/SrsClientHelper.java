package com.intellif.vesionbook.srstask.helper;

import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.enums.SrsReturnCodeEnum;
import com.intellif.vesionbook.srstask.feign.SrsClient;
import com.intellif.vesionbook.srstask.model.vo.rsp.BaseSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetClientsFromSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetGBDataFromSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SrsClientHelper {

    @Resource
    private SrsClient srsClient;

    @Resource
    private ServerConfig serverConfig;

    public List<GetStreamFromSrsRspVo.StreamData> getAllStreams() {
        GetStreamFromSrsRspVo streamResponse = srsClient.getStreams();
        if(streamResponse.getCode() != 0) {
            log.error("srs get streams return error. response: {}", streamResponse);
            return null;
        }

        List<GetStreamFromSrsRspVo.StreamData> streams = streamResponse.getStreams();
        if(streams == null || streams.isEmpty()) {
            return new ArrayList<>();
        }

        return streams;
    }

    public List<GetStreamFromSrsRspVo.StreamData> getStreamsWithNoClients() {
        List<GetStreamFromSrsRspVo.StreamData> streams = getAllStreams();
        if(streams == null || streams.isEmpty()) {
            return streams;
        }

        List<GetClientsFromSrsRspVo.ClientData> clients = getAllClients();

        if(clients == null) {
            return null;
        }

        if(clients.isEmpty()) {
            return streams;
        }

        Set<String> streamSet = clients.stream().filter(item -> !item.getPublish())
                .map(GetClientsFromSrsRspVo.ClientData::getStream).collect(Collectors.toSet());


        return streams.stream().filter(item -> !streamSet.contains(item.getId())).collect(Collectors.toList());

    }

    public List<GetGBDataFromSrsRspVo.ChannelData> getGBChannels() {

        GetGBDataFromSrsRspVo channelResponse = srsClient.getGBData("query_channel", null, null);
        if(channelResponse.getCode() != 0) {
            log.error("srs get channel return error. response: {}", channelResponse);
            return null;
        }

        List<GetGBDataFromSrsRspVo.ChannelData> channels;
        if(channelResponse.getData() == null) {
            channels = new ArrayList<>();
        } else {
            channels = channelResponse.getData().getChannels();
        }

        return channels;
    }

    public Boolean closeChannel(String id, String chid) {

        GetGBDataFromSrsRspVo response = srsClient.getGBData("sip_bye", id, chid);

        if(response.getCode() != 0) {
            log.error("srs stop channel return error. response: {}", response);
            return false;
        }

        return true;
    }

    public GetGBDataFromSrsRspVo.ChannelData getGBChannelOne(String app, String deviceId, String channelId) {
        GetGBDataFromSrsRspVo response;
        if(channelId!=null&&!channelId.isEmpty()) {
            response = srsClient.getGBData("query_channel", deviceId + "@" + channelId, null);
        }else{
            response = srsClient.getGBData("query_channel", null, null);
        }
        if(response.getCode() != 0) {
            log.error("query channel error. response: {}", response);
            return null;
        }

        List<GetGBDataFromSrsRspVo.ChannelData> channels = response.getData().getChannels();
        channels = channels.stream().filter(item -> item.getApp().equals(app) && item.getStream().startsWith(deviceId + "@")).collect(Collectors.toList());
        if(channels.isEmpty()) {
            return null;
        }
        log.info("app: {}, device id: {}, channel id: {}, get channels: {}", app, deviceId, channelId, channels);
        return channels.get(0);
    }

    /**
     *
     * @param app
     * @param deviceId
     * @param channelId
     * @return 0:成功 -1:失败 1:客户端数量超限
     */
    public Integer inviteChannel(String app, String deviceId, String channelId) {
        GetGBDataFromSrsRspVo response;
        if(deviceId==null|| deviceId.isEmpty() || channelId ==null || channelId.isEmpty()) {
            log.error("param error. device id: {}, channel id: {}", deviceId, channelId);
            return -1;
        }
        response = srsClient.getGBData("sip_query_session", deviceId, null);

        if(response.getCode() != 0) {
            log.error("query device session error, device id: {}. response: {}", deviceId, response);
            return -1;
        }

        List<GetGBDataFromSrsRspVo.SessionData> sessions = response.getData().getSessions();
        if(sessions == null || sessions.isEmpty()) {
            log.error("device not registered. please register first. sessions: {}", sessions);
            return -1;
        }

        List<GetGBDataFromSrsRspVo.DeviceData> devices = sessions.get(0).getDevices();
        if(devices==null || devices.isEmpty()) {
            log.error("device not registered. please register first. devices: {}", devices);
            return -1;
        }
        boolean inviteOk = devices.get(0).getInvite_status().equals("InviteOk");
        if(inviteOk) {
            return 0;
        }

        Integer clientsNum = getClientsNum();
        if(clientsNum == null) {
            return -1;
        }
        if(clientsNum >= serverConfig.getClientsLimit()) {
            log.error("invite client limit. device id: {}, channel id: {}, client num: {}", deviceId, channelId, clientsNum);
            return 1;
        }
        // 新增推流，通知设备推流
        GetGBDataFromSrsRspVo getGBDataFromSrsRspVo = srsClient.getGBData("sip_invite", deviceId, channelId);
        if(getGBDataFromSrsRspVo.getCode() == SrsReturnCodeEnum.ERROR_SIP_INVITE_SUCCESS_BEFORE.getResultCode()) {
            log.info("invite success before. device id: {}, channel id: {}, response: {}", deviceId, channelId, getGBDataFromSrsRspVo);
            return 0;
        }

        if(getGBDataFromSrsRspVo.getCode() != 0) {
            log.error("invite error. device id: {}, channel id: {}, response: {}", deviceId, channelId, getGBDataFromSrsRspVo);
            return -1;
        }

        log.info("invite success. device id: {}, channel id: {}, response: {}", deviceId, channelId, getGBDataFromSrsRspVo);

        return 0;
    }

    public Boolean kickOffClient(String clientId) {
        BaseSrsRspVo data = srsClient.kickOffClient(clientId);
        log.info("data: {}", data);
        if(data.getCode() == 0) {
            return true;
        }
        log.error("kick client failed. response: {}", data);
        return false;

    }

    public List<GetClientsFromSrsRspVo.ClientData> getAllClients() {
        GetClientsFromSrsRspVo clientResponse = srsClient.getClients("0", serverConfig.getClientsLimit().toString());
        if(clientResponse.getCode() != 0) {
            log.error("srs get clients return error. response: {}", clientResponse);
            return null;
        }

        List<GetClientsFromSrsRspVo.ClientData> clients = clientResponse.getClients();
        if(clients == null || clients.isEmpty()) {
            return new ArrayList<>();
        }

        return clients;
    }

    public List<GetGBDataFromSrsRspVo.SessionData> getAllGBSessions() {
        GetGBDataFromSrsRspVo response = srsClient.getGBData("sip_query_session", null, null);

        if(response.getCode() != 0) {
            log.error("query all session error. response: {}", response);
            return null;
        }

        List<GetGBDataFromSrsRspVo.SessionData> sessions = response.getData().getSessions();
        if(sessions == null) {
            log.info("get session null. response: {}", response);
            return new ArrayList<>();
        }

        return sessions;

    }

    public Integer getClientsNum() {
        List<GetClientsFromSrsRspVo.ClientData> clients = getAllClients();
        if(clients == null) {
            return null;
        }

        // 播放端客户端数量 + 除了gb28181的发布端数量
        int total = clients.size();

        // gb28181发布端数量
        List<GetGBDataFromSrsRspVo.SessionData> sessions = getAllGBSessions();
        if(sessions == null) {
            return null;
        }

        int gbCount = 0;
        for (GetGBDataFromSrsRspVo.SessionData session : sessions) {
            List<GetGBDataFromSrsRspVo.DeviceData> devices = session.getDevices();
            if(devices==null || devices.isEmpty()) {
                continue;
            }
            long count = devices.stream().filter(item -> item.getInvite_status().equals("InviteOk") && item.getDevice_status().equals("ON")).count();
            gbCount += count;
        }

        return total + gbCount;
    }

    public GetStreamFromSrsRspVo.StreamData getStreamOne(String app, String uniqueId) {
        List<GetStreamFromSrsRspVo.StreamData> streams = getAllStreams();
        if(streams == null || streams.isEmpty()) {
            log.error("query gb streams list null. unique id: {}", uniqueId);
            return null;
        }

        List<GetStreamFromSrsRspVo.StreamData> channelStreams = streams.stream()
                .filter(item -> item.getApp().equals(app) && item.getName().equals(uniqueId))
                .collect(Collectors.toList());
        if(channelStreams.isEmpty()) {
            log.error("query gb stream null. unique id: {}", uniqueId);
            return null;
        }

        if(channelStreams.size() > 1) {
            log.error("query gb stream multi. unique id: {}, result: {}", uniqueId, channelStreams);
            return null;
        }

        return channelStreams.get(0);

    }

}
