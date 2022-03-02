package com.intellif.vesionbook.srstask.service.impl;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.enums.StreamOutputTypeEnum;
import com.intellif.vesionbook.srstask.helper.FFCommandHelper;
import com.intellif.vesionbook.srstask.helper.JavaCVHelper;
import com.intellif.vesionbook.srstask.helper.SrsClientHelper;
import com.intellif.vesionbook.srstask.model.vo.rsp.StreamTaskRspVo;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.*;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetGBDataFromSrsRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetStreamFromSrsRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Resource
    ServerConfig serverConfig;

    @Resource
    StreamTaskCache streamTaskCache;

    @Resource
    JavaCVHelper javaCVHelper;

    @Resource
    FFCommandHelper ffCommandHelper;

    @Resource
    SrsClientHelper srsClientHelper;


    @Override
    @Transactional
    public BaseResponseVo<StreamTaskRspVo> getOrCreateStreamTask(TaskReqVo taskReqVo) {

        String app = taskReqVo.getApp();
        String uniqueId = taskReqVo.getUniqueId();
        String originStream = taskReqVo.getOriginStream();
        Integer outputType = taskReqVo.getOutputType();

        if (outputType != StreamOutputTypeEnum.RTMP.getCode() && outputType != StreamOutputTypeEnum.HTTP_HLV.getCode()
                && outputType != StreamOutputTypeEnum.WEB_RTC.getCode() && outputType != StreamOutputTypeEnum.HLS.getCode()) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }

        StreamTaskRspVo vo = getOutputStream(taskReqVo.getApp(), taskReqVo.getUniqueId(), outputType);

        Boolean exists = checkCacheAliveOrClearDead(taskReqVo.getApp(), taskReqVo.getUniqueId());
        if (exists) {
            return BaseResponseVo.ok(vo);
        }

        // -------------- 开始创建 ---------------------
        Integer leftStreamSpace = getLeftStreamSpace();
        if (leftStreamSpace <= 0) {
            log.error("over task limit. app: {}, unique id: {}", app, uniqueId);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_MAX_LIMIT);
        }
        // 创建任务
        Boolean success = createCacheTask(originStream, app, uniqueId);
        if (!success) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_MAX_LIMIT);
        }

        return BaseResponseVo.ok(vo);
    }

    public Boolean checkCacheAliveOrClearDead(String app, String uniqueId) {
        if (Objects.equals(serverConfig.getUseJavacv(), "1")) {
            Thread process = streamTaskCache.getThread(app, uniqueId);
            log.info("app: {}, unique id: {}, process is null: {}, process is alive: {}",
                    app, uniqueId, process == null, process != null && process.isAlive());
            if (process != null && process.isAlive()) {
                return true;
            }
        } else {
            Process process = streamTaskCache.getProcess(app, uniqueId);
            log.info("app: {}, unique id: {}, process is null: {}, process is alive: {}",
                    app, uniqueId, process == null, process != null && process.isAlive());
            if (process != null && process.isAlive()) {
                return true;
            }
        }
        closeCache(app, uniqueId);

        return false;
    }

    public Boolean createCacheTask(String originStream, String app, String uniqueId) {
        log.info("create cache task. app: {}, unique id: {}, origin stream: {}", app, uniqueId, originStream);

        // 创建流任务
        if (serverConfig.getUseJavacv().equals("1")) {
            javaCVHelper.asyncPullRtspPushRtmp(originStream, app, uniqueId);
        } else {
            Process ffmpeg = ffCommandHelper.transcodeStream(originStream, app, uniqueId, serverConfig.getSrsHost(), null);
            if (ffmpeg == null) {
                return false;
            }
            streamTaskCache.storeProcess(app, uniqueId, ffmpeg);
        }

        return true;
    }

    public StreamTaskRspVo getOutputStream(String app, String uniqueId, Integer outputType) {
        StreamTaskRspVo vo = new StreamTaskRspVo();
        if (outputType == StreamOutputTypeEnum.RTMP.getCode()) {
            vo.setRtmpOutput("rtmp://" + serverConfig.getOutputHost() + "/" + app + "/" + uniqueId);
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode()) {
            vo.setHttpFlvOutput("http://" + serverConfig.getOutputHost() + ":" + serverConfig.getHttpOutputPort() + "/" + app + "/" + uniqueId + ".flv");
        } else if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode()) {
            vo.setWebrtcOutput("webrtc://" + serverConfig.getOutputHost() + "/" + app + "/" + uniqueId);
        } else if (outputType == StreamOutputTypeEnum.HLS.getCode()) {
            vo.setHlsOutput("http://" + serverConfig.getOutputHost() + ":" + serverConfig.getHttpOutputPort() + "/" + app + "/" + uniqueId + ".m3u8");
        } else {
            return null;
        }
        return vo;
    }

    @Override
    @Transactional
    public BaseResponseVo<String> closeRtspStreamTask(CloseTaskReqVo closeTaskReqVo) {
        String app = closeTaskReqVo.getApp();
        String uniqueId = closeTaskReqVo.getUniqueId();
        // 关闭缓存
        closeCache(app, uniqueId);
        return BaseResponseVo.ok();
    }


    public Integer getLeftStreamSpace() {
        Integer existsTask;
        if (serverConfig.getUseJavacv().equals("1")) {
            existsTask = streamTaskCache.getThreadNumber();
        } else {
            existsTask = streamTaskCache.getProcessNumber();
        }
        return serverConfig.getStreamPoolLimit() - existsTask;

    }

    @Override
    @Transactional
    public BaseResponseVo<String> syncStreamTask(SyncReqVo syncReqVo) {
        String app = syncReqVo.getApp();

        List<TaskReqVo> reqTaskList = syncReqVo.getAliveTaskList();
        List<String> reqUniqueIdList = reqTaskList.stream().map(TaskReqVo::getUniqueId).collect(Collectors.toList());
        // 关闭缓存中不该存在的流任务
        closeCacheFromClient(app, reqUniqueIdList);

        // 开启缓存中应该存在的流任务
        if (getLeftStreamSpace() <= 0) {
            log.error("sync error. There is no space left.");
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_MAX_LIMIT);
        }

        startCacheFromClient(app, reqTaskList);

        return BaseResponseVo.ok();
    }

    public void closeCacheFromClient(String app, List<String> uniqueIdList) {
        if (serverConfig.getUseJavacv().equals("1")) {
            List<String> shouldAliveList = uniqueIdList.stream().map(item -> streamTaskCache.getTaskThreadKey(app, item)).collect(Collectors.toList());
            ConcurrentHashMap<String, Thread> threadMap = streamTaskCache.getThreadMap();
            for (Map.Entry<String, Thread> entry : threadMap.entrySet()) {
                String uniqueId = entry.getKey();
                if (!shouldAliveList.contains(uniqueId)) {
                    closeCache(app, uniqueId);
                }
            }
        } else {
            List<String> shouldAliveList = uniqueIdList.stream().map(item -> streamTaskCache.getTaskKey(app, item)).collect(Collectors.toList());
            ConcurrentHashMap<String, Process> threadMap = streamTaskCache.getProcessMap();
            for (Map.Entry<String, Process> entry : threadMap.entrySet()) {
                String uniqueId = entry.getKey();
                if (!shouldAliveList.contains(uniqueId)) {
                    closeCache(app, uniqueId);
                }
            }

        }
    }

    public void closeCache(String app, String uniqueId) {
        if (serverConfig.getUseJavacv().equals("1")) {
            Thread thread = streamTaskCache.getThread(app, uniqueId);
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            streamTaskCache.clearThread(app, uniqueId);
        } else {
            Process process = streamTaskCache.getProcess(app, uniqueId);
            if (process != null && process.isAlive()) {
                process.destroy();
                log.info("close cache. app: {}, uniqueId: {}", app, uniqueId);
            }
            streamTaskCache.clearProcess(app, uniqueId);
        }
    }

    public void startCacheFromClient(String app, List<TaskReqVo> taskVos) {
        if (serverConfig.getUseJavacv().equals("1")) {
            ConcurrentHashMap<String, Thread> threadMap = streamTaskCache.getThreadMap();
            Set<String> cacheList = threadMap.keySet();

            for (TaskReqVo taskVo : taskVos) {
                String key = streamTaskCache.getTaskThreadKey(app, taskVo.getUniqueId());

                if (!cacheList.contains(key)) {
                    createCacheTask(taskVo.getOriginStream(), app, taskVo.getUniqueId());
                }
            }
        } else {
            ConcurrentHashMap<String, Process> processMap = streamTaskCache.getProcessMap();
            Set<String> cacheList = processMap.keySet();

            for (TaskReqVo taskVo : taskVos) {
                String key = streamTaskCache.getTaskKey(app, taskVo.getUniqueId());

                if (!cacheList.contains(key)) {
                    createCacheTask(taskVo.getOriginStream(), app, taskVo.getUniqueId());
                }
            }

        }
    }

    @Override
    public BaseResponseVo<List<StreamTaskRspVo>> aliveStreamTaskList(TaskListReqVo taskListReqVo) {
        List<GetStreamFromSrsRspVo.StreamData> aliveStreams = srsClientHelper.getAliveStreams();
        List<StreamTaskRspVo> taskList = aliveStreams.stream().map(item -> {
            StreamTaskRspVo task = new StreamTaskRspVo();
            task.setApp(item.getApp());
            task.setUniqueId(item.getName());
            return task;
        }).collect(Collectors.toList());

        List<StreamTaskRspVo> aliveTaskList = taskList.stream().filter(
                item -> checkCacheAliveOrClearDead(item.getApp(), item.getUniqueId())).collect(Collectors.toList());
        return BaseResponseVo.ok(aliveTaskList);
    }

    /**
     * 获取gb28181视频流
     *
     * @param taskReqVo
     * @return
     */
    @Override
    public BaseResponseVo<StreamTaskRspVo> getGBStream(TaskReqVo taskReqVo) {
        if (taskReqVo.getChannelId() == null || taskReqVo.getChannelId().isEmpty()) {
            log.error("req error, channel id is empty. req: {}", taskReqVo);
            return BaseResponseVo.error(ReturnCodeEnum.PARAM_INVALID);
        }
        // srs服务器gb28181分支暂时不支持自定义app，固定live
        taskReqVo.setApp("live");

        Integer result = srsClientHelper.inviteChannel(taskReqVo.getApp(), taskReqVo.getUniqueId(), taskReqVo.getChannelId());
        if (result == -1) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_FAILED);
        }
        if (result == 1) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_CLIENTS_LIMIT);
        }

        GetGBDataFromSrsRspVo.ChannelData gbChannelOne = srsClientHelper.getGBChannelOne(taskReqVo.getApp(),
                taskReqVo.getUniqueId(), taskReqVo.getChannelId());
        if (gbChannelOne == null) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_GB_CHANNEL);
        }

        StreamTaskRspVo vo = new StreamTaskRspVo();
        Integer outputType = taskReqVo.getOutputType();
        if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode()) {
            vo.setWebrtcOutput(gbChannelOne.getWebrtc_url());
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode()) {
            vo.setHttpFlvOutput(gbChannelOne.getFlv_url());
        } else if (outputType == StreamOutputTypeEnum.HLS.getCode()) {
            vo.setHlsOutput(gbChannelOne.getHls_url());
        } else if (outputType == StreamOutputTypeEnum.RTMP.getCode()) {
            vo.setRtmpOutput(gbChannelOne.getRtmp_url());
        } else {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }

        return BaseResponseVo.ok(vo);
    }

    @Override
    public BaseResponseVo<GetStreamFromSrsRspVo.StreamData> getStreamInfo(StreamInfoReqVo streamInfoReqVo) {
        // srs gb28181版本只支持app=live
        streamInfoReqVo.setApp("live");
        String streamName;
        if (streamInfoReqVo.getChannelId() != null && !streamInfoReqVo.getChannelId().isEmpty()) {
            streamName = streamInfoReqVo.getUniqueId() + "@" + streamInfoReqVo.getChannelId();
        } else {
            streamName = streamInfoReqVo.getUniqueId();
        }

        // 获取分辨率
        GetStreamFromSrsRspVo.StreamData streamOne = srsClientHelper.getStreamOne(streamInfoReqVo.getApp(), streamName);
        if (streamOne == null) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_ONE_NULL);
        }

        return BaseResponseVo.ok(streamOne);
    }
}
