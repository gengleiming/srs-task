package com.intellif.vesionbook.srstask.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamTaskCache {
    private final ConcurrentHashMap<String, Process> taskMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Thread> taskThreadMap = new ConcurrentHashMap<>();

    private String getTaskKey(String app, String uniqueId) {
        return "stream_task_" + app + "_" + uniqueId;
    }

    public void storeProcess(String app, String uniqueId, Process process) {
        String taskKey = getTaskKey(app, uniqueId);
        taskMap.put(taskKey, process);
    }

    public Process getProcess(String app, String uniqueId) {
        String taskKey = getTaskKey(app, uniqueId);
        return taskMap.get(taskKey);
    }

    private String getTaskThreadKey(String app, String uniqueId) {
        return "stream_task_" + app + "_" + uniqueId;
    }

    public void storeThread(String app, String uniqueId, Thread process) {
        String taskKey = getTaskThreadKey(app, uniqueId);
        taskThreadMap.put(taskKey, process);
    }

    public Thread getThread(String app, String uniqueId) {
        String taskKey = getTaskThreadKey(app, uniqueId);
        return taskThreadMap.get(taskKey);
    }
    public void clearThread(String app, String uniqueId) {
        String taskKey = getTaskThreadKey(app, uniqueId);
        taskThreadMap.remove(taskKey);
    }
}
