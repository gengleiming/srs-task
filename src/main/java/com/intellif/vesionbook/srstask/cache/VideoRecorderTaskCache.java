package com.intellif.vesionbook.srstask.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class VideoRecorderTaskCache {
    private final ConcurrentHashMap<String, Process> taskMap = new ConcurrentHashMap<>();

    public String getTaskKey(String app, String uniqueId) {
        return "recorder_" + app + "_" + uniqueId;
    }

    public void storeProcess(String app, String uniqueId, Process process) {
        String taskKey = getTaskKey(app, uniqueId);
        taskMap.put(taskKey, process);
    }

    public Process getProcess(String app, String uniqueId) {
        String taskKey = getTaskKey(app, uniqueId);
        return taskMap.get(taskKey);
    }

    public void clearProcess(String app, String uniqueId) {
        String taskKey = getTaskKey(app, uniqueId);
        taskMap.remove(taskKey);
    }

    public boolean existProcess(String app, String uniqueId) {
        String taskKey = getTaskKey(app, uniqueId);
        Process process = taskMap.get(taskKey);
        if(process == null || !process.isAlive()) {
            return false;
        }
        return true;
    }

    public int getProcessNumber() {
        return taskMap.size();
    }

    public ConcurrentHashMap<String, Process> getProcessMap() {
        return taskMap;
    }

}
