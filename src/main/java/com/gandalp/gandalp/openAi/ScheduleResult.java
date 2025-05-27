package com.gandalp.gandalp.openAi;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class ScheduleResult {
    private boolean success;
    private Map<String, Map<String, String>> schedule;
    private String failureReason;
}
