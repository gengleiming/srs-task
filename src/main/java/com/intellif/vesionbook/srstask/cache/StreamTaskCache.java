package com.intellif.vesionbook.srstask.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


public class StreamTaskCache {
    public static ConcurrentHashMap<String, Process> taskMap = new ConcurrentHashMap<>();

    public static String getTaskKey(String app, String uniqueId) {
        return app + "-" + uniqueId;
    }

    public static Process getProcess(String app, String uniqueId) {
        String taskKey = getTaskKey(app, uniqueId);
        return taskMap.get(taskKey);
    }

    public static void storeProcess(String app, String uniqueId, Process process) {
        String taskKey = getTaskKey(app, uniqueId);
        taskMap.put(taskKey, process);

    }
}
