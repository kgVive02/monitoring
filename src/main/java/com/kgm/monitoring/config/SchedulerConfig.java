package com.kgm.monitoring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 스케줄러 설정
 * - SNMP 폴링 스케줄러 (3~10초 주기)
 * - 일별 집계 배치 스케줄러 (매일 00:05)
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // TODO: Task 2.2, 2.5에서 구현 예정
    }
}