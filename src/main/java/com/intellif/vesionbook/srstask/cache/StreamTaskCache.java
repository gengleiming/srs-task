package com.intellif.vesionbook.srstask.cache;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamTaskCache {
    public static ConcurrentHashMap<String, Process> taskMap = new ConcurrentHashMap<>();
}
