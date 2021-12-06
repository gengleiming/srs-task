package com.intellif.vesionbook.srstask.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamTaskCache {
    public static ConcurrentHashMap<String, Process> taskMap = new ConcurrentHashMap<>();

    public static String getTaskKey(String app, String uniqueId) {
        return "stream_task_" + app + "_" + uniqueId;
    }

    public static void storeProcess(String app, String uniqueId, Process process) {
        String taskKey = getTaskKey(app, uniqueId);
        taskMap.put(taskKey, process);
    }

    public static Process getProcess(String app, String uniqueId) {
        String taskKey = getTaskKey(app, uniqueId);
        return taskMap.get(taskKey);
    }
}
