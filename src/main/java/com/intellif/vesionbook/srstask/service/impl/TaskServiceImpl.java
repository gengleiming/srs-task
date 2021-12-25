package com.intellif.vesionbook.srstask.service.impl;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.enums.StreamOutputTypeEnum;
import com.intellif.vesionbook.srstask.enums.StreamTaskStatusEnum;
import com.intellif.vesionbook.srstask.helper.FFCommandHelper;
import com.intellif.vesionbook.srstask.helper.JavaCVHelper;
import com.intellif.vesionbook.srstask.helper.SrsClientHelper;
import com.intellif.vesionbook.srstask.mapper.StreamTaskMapper;
import com.intellif.vesionbook.srstask.model.dto.StreamTaskDto;
import com.intellif.vesionbook.srstask.model.entity.StreamTask;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskListReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.TaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.CloseTaskReqVo;
import com.intellif.vesionbook.srstask.model.vo.req.SyncReqVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.CreateTaskRspVo;
import com.intellif.vesionbook.srstask.model.vo.rsp.GetGBDataFromSrsRspVo;
import com.intellif.vesionbook.srstask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Resource
    StreamTaskMapper streamTaskMapper;

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
    public BaseResponseVo<CreateTaskRspVo> getOrCreateStreamTask(TaskReqVo taskReqVo) {

        String app = taskReqVo.getApp();
        String uniqueId = taskReqVo.getUniqueId();
        String originStream = taskReqVo.getOriginStream();
        Integer outputType = taskReqVo.getOutputType();

        if (outputType != StreamOutputTypeEnum.RTMP.getCode() && outputType != StreamOutputTypeEnum.HTTP_HLV.getCode()
                && outputType != StreamOutputTypeEnum.WEB_RTC.getCode() && outputType != StreamOutputTypeEnum.HLS.getCode()) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }

        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId).service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).lock(true).build();
        List<StreamTask> tasks = getStreamTask(streamTaskDto);
        if (tasks.size() > 1) {
            log.error("发现重复的流任务 stream tasks: {}", tasks);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_REPEAT);
        }

        if (tasks.size() == 1) {
            StreamTask task = tasks.get(0);
            Boolean exists = checkCacheAliveOrClearDead(task);
            if (exists) {
                return getStreamAddress(task, outputType);
            }
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

        String rtmpOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.RTMP.getCode());
        String httpFlvOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.HTTP_HLV.getCode());
        String webrtcOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.WEB_RTC.getCode());
        String hlsOutput = getOutputStream(app, uniqueId, StreamOutputTypeEnum.HLS.getCode());
        StreamTask task = StreamTask.builder().app(app).uniqueId(uniqueId).originStream(originStream)
                .service(serverConfig.getServiceId()).status(StreamTaskStatusEnum.PROCESSING.getCode())
                .rtmpOutput(rtmpOutput).httpFlvOutput(httpFlvOutput).hlsOutput(hlsOutput).webrtcOutput(webrtcOutput)
                .forever(0).build();

        if (tasks.size() == 0) {
            streamTaskMapper.insertSelective(task);
        }

        return getStreamAddress(task, outputType);
    }

    public Boolean checkCacheAliveOrClearDead(StreamTask task) {
        if (Objects.equals(serverConfig.getUseJavacv(), "1")) {
            Thread process = streamTaskCache.getThread(task.getApp(), task.getUniqueId());
            log.info("app: {}, unique id: {}, process is null: {}, process is alive: {}",
                    task.getApp(), task.getUniqueId(), process == null, process != null && process.isAlive());
            if (process != null && process.isAlive()) {
                return true;
            }
        } else {
            Process process = streamTaskCache.getProcess(task.getApp(), task.getUniqueId());
            log.info("app: {}, unique id: {}, process is null: {}, process is alive: {}",
                    task.getApp(), task.getUniqueId(), process == null, process != null && process.isAlive());
            if (process != null && process.isAlive()) {
                return true;
            }
        }
        closeCache(task.getApp(), task.getUniqueId());

        return false;
    }

    public Boolean createCacheTask(String originStream, String app, String uniqueId) {
        log.info("create cache task. app: {}, unique id: {}, origin stream: {}", app, uniqueId, originStream);

        // 创建流任务
        if (serverConfig.getUseJavacv().equals("1")) {
            javaCVHelper.asyncPullRtspPushRtmp(originStream, app, uniqueId);
        } else {
            Process ffmpeg = ffCommandHelper.transcodeStream(originStream, app, uniqueId);
            if (ffmpeg == null) {
                return false;
            }
            streamTaskCache.storeProcess(app, uniqueId, ffmpeg);
        }

        return true;
    }

    public String getOutputStream(String app, String uniqueId, Integer outputType) {
        String output;
        if (outputType == StreamOutputTypeEnum.RTMP.getCode()) {
            output = "rtmp://" + serverConfig.getOutputHost() + "/" + app + "/" + uniqueId;
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode()) {
            output = "http://" + serverConfig.getOutputHost() + ":" + serverConfig.getHttpOutputPort() + "/" + app + "/" + uniqueId + ".flv";
        } else if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode()) {
            output = "webrtc://" + serverConfig.getOutputHost() + "/" + app + "/" + uniqueId;
        } else if (outputType == StreamOutputTypeEnum.HLS.getCode()) {
            output = "http://" + serverConfig.getOutputHost() + ":" + serverConfig.getHttpOutputPort() + "/" + app + "/" + uniqueId + ".m3u8";
        } else {
            return null;
        }
        return output;
    }

    public BaseResponseVo<CreateTaskRspVo> getStreamAddress(StreamTask task, Integer outputType) {

        CreateTaskRspVo vo;
        if (outputType == StreamOutputTypeEnum.RTMP.getCode() && !StringUtils.isEmpty(task.getRtmpOutput())) {
            vo = CreateTaskRspVo.builder().rtmpOutput(task.getRtmpOutput()).build();
        } else if (outputType == StreamOutputTypeEnum.HTTP_HLV.getCode() && !StringUtils.isEmpty(task.getHttpFlvOutput())) {
            vo = CreateTaskRspVo.builder().httpFlvOutput(task.getHttpFlvOutput()).build();
        } else if (outputType == StreamOutputTypeEnum.WEB_RTC.getCode() && !StringUtils.isEmpty(task.getWebrtcOutput())) {
            vo = CreateTaskRspVo.builder().webrtcOutput(task.getWebrtcOutput()).build();
        } else if (outputType == StreamOutputTypeEnum.HLS.getCode() && !StringUtils.isEmpty(task.getHlsOutput())) {
            vo = CreateTaskRspVo.builder().hlsOutput(task.getHlsOutput()).build();
        } else {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }
        return BaseResponseVo.ok(vo);
    }

    @Override
    @Transactional
    public BaseResponseVo<String> closeRtspStreamTask(CloseTaskReqVo closeTaskReqVo) {
        String app = closeTaskReqVo.getApp();
        String uniqueId = closeTaskReqVo.getUniqueId();
        String originStream = closeTaskReqVo.getOriginStream();

        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId).service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).originStream(originStream).lock(true).build();

        List<StreamTask> tasks = getStreamTask(streamTaskDto);

        // 关闭缓存
        closeCache(app, uniqueId);

        // 关闭db
        if (tasks.isEmpty()) {
            log.info("数据库未发现该流任务 app: {}, uniqueId: {}, originStream: {}", app, uniqueId, originStream);
            return BaseResponseVo.ok();
        }

        if (tasks.size() > 1) {
            log.error("发现重复的流任务 stream tasks: {}", tasks);
        }

        // 关闭
        StreamTaskDto taskDto = StreamTaskDto.builder().app(app).uniqueId(uniqueId).service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.CLOSED.getCode()).build();
        streamTaskMapper.updateStatus(taskDto);

        return BaseResponseVo.ok();
    }

    @Override
    public Integer recoverForeverStreamTask() {
        StreamTaskDto streamTaskDto = StreamTaskDto.builder().service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).forever(1).lock(true).build();

        List<StreamTask> tasks = getStreamTask(streamTaskDto);

        if (tasks.isEmpty()) {
            return 0;
        }

        int success = 0;
        for (StreamTask task : tasks) {
            Boolean ok = recoverTask(task);
            if (ok) {
                success += 1;
            }
        }
        return success;
    }

    public Boolean recoverTask(StreamTask task) {
        if (serverConfig.getUseJavacv().equals("1")) {
            Thread process = streamTaskCache.getThread(task.getApp(), task.getUniqueId());
            if (process == null || !process.isAlive()) {
                javaCVHelper.asyncPullRtspPushRtmp(task.getOriginStream(), task.getApp(), task.getUniqueId());
                return true;
            }

        } else {
            Process process = streamTaskCache.getProcess(task.getApp(), task.getUniqueId());
            if (process == null || !process.isAlive()) {
                Process ffmpeg = ffCommandHelper.transcodeStream(task.getOriginStream(), task.getApp(), task.getUniqueId());
                if (ffmpeg == null) {
                    return false;
                }
                streamTaskCache.storeProcess(task.getApp(), task.getUniqueId(), ffmpeg);
                return true;
            }
        }
        return false;
    }

    /**
     * 服务启动的时候，关闭旧任务
     * @return
     */
    @Override
    public Integer cleanOldTaskWhileStart() {
        StreamTaskDto streamTaskDto = StreamTaskDto.builder().service(serverConfig.getServiceId())
                .oldStatus(StreamTaskStatusEnum.PROCESSING.getCode()).status(StreamTaskStatusEnum.CLOSED.getCode()).lock(true).build();

        return streamTaskMapper.updateStatus(streamTaskDto);

    }

    public Boolean closeDeadTask(StreamTask task) {
        if (serverConfig.getUseJavacv().equals("1")) {
            Thread thread = streamTaskCache.getThread(task.getApp(), task.getUniqueId());

            if (thread == null || !thread.isAlive()) {
                // 关闭
                StreamTaskDto updateTask = StreamTaskDto.builder().id(task.getId()).status(StreamTaskStatusEnum.CLOSED.getCode()).build();
                streamTaskMapper.updateStatus(updateTask);
                streamTaskCache.clearThread(task.getApp(), task.getUniqueId());
                return true;
            }

        } else {
            Process process = streamTaskCache.getProcess(task.getApp(), task.getUniqueId());

            if (process == null || !process.isAlive()) {
                // 关闭
                StreamTaskDto updateTask = StreamTaskDto.builder().id(task.getId()).status(StreamTaskStatusEnum.CLOSED.getCode()).build();
                streamTaskMapper.updateStatus(updateTask);
                streamTaskCache.clearProcess(task.getApp(), task.getUniqueId());
                return true;
            }
        }
        return false;
    }

    public List<StreamTask> getStreamTask(StreamTaskDto streamTaskDto) {
        List<StreamTask> streamTasks = streamTaskMapper.selectByParam(streamTaskDto);
        if (streamTasks == null) {
            return new ArrayList<>();
        }

        if (streamTasks.size() == 0) {
            return new ArrayList<>();
        }

        return streamTasks;
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

        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(app).service(serverConfig.getServiceId())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).lock(true).build();

        List<StreamTask> dbAliveTasks = getStreamTask(streamTaskDto);

        List<TaskReqVo> reqTaskList = syncReqVo.getAliveTaskList();
        List<String> reqUniqueIdList = reqTaskList.stream().map(TaskReqVo::getUniqueId).collect(Collectors.toList());
        // 先关闭缓存中不该存在的流任务
        closeCacheFromClient(app, reqUniqueIdList);
        // 关闭数据库不该存在的任务
        List<Long> shouldDeadIdList = dbAliveTasks.stream().filter(item->!reqUniqueIdList.contains(item.getUniqueId())).
                map(StreamTask::getId).collect(Collectors.toList());
        if(shouldDeadIdList.size()>0) {
            StreamTaskDto updateTask = StreamTaskDto.builder().idList(shouldDeadIdList)
                    .status(StreamTaskStatusEnum.CLOSED.getCode()).build();
            streamTaskMapper.updateStatus(updateTask);
        }

        // 开启缓存中应该存在的流任务
        if(getLeftStreamSpace() <= 0) {
            log.error("sync error. There is no space left.");
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_MAX_LIMIT);
        }

        startCacheFromClient(app, reqTaskList);

        // 批量插入应该存活的任务
        List<String> dbAliveUniqueList = dbAliveTasks.stream().map(StreamTask::getUniqueId).collect(Collectors.toList());
        List<TaskReqVo> voList = syncReqVo.getAliveTaskList().stream().filter(item -> !dbAliveUniqueList.contains(item.getUniqueId()))
                .collect(Collectors.toList());
        List<StreamTask> insertList = new ArrayList<>();
        for(TaskReqVo vo: voList) {
            String rtmpOutput = getOutputStream(app, vo.getUniqueId(), StreamOutputTypeEnum.RTMP.getCode());
            String httpFlvOutput = getOutputStream(app, vo.getUniqueId(), StreamOutputTypeEnum.HTTP_HLV.getCode());
            String webrtcOutput = getOutputStream(app, vo.getUniqueId(), StreamOutputTypeEnum.WEB_RTC.getCode());
            String hlsOutput = getOutputStream(app, vo.getUniqueId(), StreamOutputTypeEnum.HLS.getCode());
            StreamTask task = StreamTask.builder().originStream(vo.getOriginStream()).app(vo.getApp()).uniqueId(vo.getUniqueId())
                    .service(serverConfig.getServiceId()).status(StreamTaskStatusEnum.PROCESSING.getCode())
                    .forever(0).httpFlvOutput(httpFlvOutput).rtmpOutput(rtmpOutput).webrtcOutput(webrtcOutput).hlsOutput(hlsOutput).build();
            insertList.add(task);
        }

        log.info("insert batch: {}", insertList);
        if(insertList.size()>0) {
            streamTaskMapper.insertTaskBatch(insertList);
        }

        return BaseResponseVo.ok();
    }

    public void closeCacheFromClient(String app, List<String> uniqueIdList) {
        if(serverConfig.getUseJavacv().equals("1")) {
            List<String> shouldAliveList = uniqueIdList.stream().map(item-> streamTaskCache.getTaskThreadKey(app, item)).collect(Collectors.toList());
            ConcurrentHashMap<String, Thread> threadMap = streamTaskCache.getThreadMap();
            for(Map.Entry<String, Thread> entry: threadMap.entrySet()){
                String uniqueId = entry.getKey();
                if(!shouldAliveList.contains(uniqueId)) {
                    closeCache(app, uniqueId);
                }
            }
        } else {
            List<String> shouldAliveList = uniqueIdList.stream().map(item-> streamTaskCache.getTaskKey(app, item)).collect(Collectors.toList());
            ConcurrentHashMap<String, Process> threadMap = streamTaskCache.getProcessMap();
            for(Map.Entry<String, Process> entry: threadMap.entrySet()){
                String uniqueId = entry.getKey();
                if(!shouldAliveList.contains(uniqueId)) {
                    closeCache(app, uniqueId);
                }
            }

        }
    }

    public void closeCache(String app, String uniqueId) {
        if(serverConfig.getUseJavacv().equals("1")) {
            Thread thread = streamTaskCache.getThread(app, uniqueId);
            if(thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            streamTaskCache.clearThread(app, uniqueId);
        } else {
            Process process = streamTaskCache.getProcess(app, uniqueId);
            if(process != null && process.isAlive()) {
                process.destroy();
            }
            streamTaskCache.clearProcess(app, uniqueId);
        }
        log.info("close cache. app: {}, unique id: {}", app, uniqueId);
    }

    public void startCacheFromClient(String app, List<TaskReqVo> taskVos) {
        if(serverConfig.getUseJavacv().equals("1")) {
            ConcurrentHashMap<String, Thread> threadMap = streamTaskCache.getThreadMap();
            Set<String> cacheList = threadMap.keySet();

            for (TaskReqVo taskVo : taskVos) {
                String key = streamTaskCache.getTaskThreadKey(app, taskVo.getUniqueId());

                if(!cacheList.contains(key)) {
                    createCacheTask(taskVo.getOriginStream(), app, taskVo.getUniqueId());
                }
            }
        } else {
            ConcurrentHashMap<String, Process> processMap = streamTaskCache.getProcessMap();
            Set<String> cacheList = processMap.keySet();

            for (TaskReqVo taskVo : taskVos) {
                String key = streamTaskCache.getTaskKey(app, taskVo.getUniqueId());

                if(!cacheList.contains(key)) {
                    createCacheTask(taskVo.getOriginStream(), app, taskVo.getUniqueId());
                }
            }

        }
    }

    @Override
    public BaseResponseVo<List<StreamTask>> aliveStreamTaskList(TaskListReqVo taskListReqVo) {
        StreamTaskDto streamTaskDto = StreamTaskDto.builder().app(taskListReqVo.getApp())
                .status(StreamTaskStatusEnum.PROCESSING.getCode()).build();
        List<StreamTask> taskList = getStreamTask(streamTaskDto);
        if(taskList == null || taskList.isEmpty()) {
            return BaseResponseVo.ok(new ArrayList<>());
        }

        List<StreamTask> aliveTaskList = taskList.stream().filter(this::checkCacheAliveOrClearDead).collect(Collectors.toList());
        return BaseResponseVo.ok(aliveTaskList);
    }

    /**
     * 获取gb28181视频流
     * @param taskReqVo
     * @return
     */
    @Override
    public BaseResponseVo<CreateTaskRspVo> getGBStream(TaskReqVo taskReqVo) {
        if(taskReqVo.getChannelId()==null||taskReqVo.getChannelId().isEmpty()) {
            log.error("req error, channel id is empty. req: {}", taskReqVo);
            return BaseResponseVo.error(ReturnCodeEnum.PARAM_INVALID);
        }
        // srs服务器gb28181分支暂时不支持自定义app，固定live
        taskReqVo.setApp("live");

        Integer result = srsClientHelper.inviteChannel(taskReqVo.getApp(), taskReqVo.getUniqueId(), taskReqVo.getChannelId());
        if(result == -1){
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_FAILED);
        }
        if(result == 1) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_CLIENTS_LIMIT);
        }

        GetGBDataFromSrsRspVo.ChannelData gbChannelOne = srsClientHelper.getGBChannelOne(taskReqVo.getApp(),
                taskReqVo.getUniqueId(), taskReqVo.getChannelId());
        if(gbChannelOne == null) {
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_GB_CHANNEL);
        }

        CreateTaskRspVo vo = new CreateTaskRspVo();
        Integer outputType = taskReqVo.getOutputType();
            if(outputType == StreamOutputTypeEnum.WEB_RTC.getCode()) {
                vo.setWebrtcOutput(gbChannelOne.getWebrtc_url());
            } else if(outputType == StreamOutputTypeEnum.HTTP_HLV.getCode()) {
                vo.setWebrtcOutput(gbChannelOne.getFlv_url());
            } else if(outputType == StreamOutputTypeEnum.HLS.getCode()) {
                vo.setWebrtcOutput(gbChannelOne.getHls_url());
            } else if(outputType == StreamOutputTypeEnum.RTMP.getCode()) {
                vo.setWebrtcOutput(gbChannelOne.getRtmp_url());
            } else {
                return BaseResponseVo.error(ReturnCodeEnum.ERROR_STREAM_TASK_TYPE_NOT_SUPPORT);
        }
        return BaseResponseVo.ok(vo);
    }
}
