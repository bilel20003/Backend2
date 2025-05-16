package com.centre.service.config;

import com.centre.service.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatisticsScheduler {

    @Autowired
    private StatisticsService statisticsService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleStatisticsAggregation() {
        statisticsService.aggregateAndSaveStatistics();
    }
}