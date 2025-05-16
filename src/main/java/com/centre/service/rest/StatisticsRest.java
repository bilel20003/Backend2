package com.centre.service.rest;

import com.centre.service.model.PeriodType;
import com.centre.service.model.Statistics;
import com.centre.service.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsRest {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/compare")
    public List<Statistics> getStatisticsForComparison(
            @RequestParam("periodType") PeriodType periodType,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return statisticsService.getStatisticsForComparison(periodType, start, end);
    }
}