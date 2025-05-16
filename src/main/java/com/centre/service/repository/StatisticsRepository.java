package com.centre.service.repository;

import com.centre.service.model.PeriodType;
import com.centre.service.model.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    List<Statistics> findByPeriodTypeAndStartDateBetween(PeriodType periodType, LocalDateTime start, LocalDateTime end);

    List<Statistics> findByPeriodTypeAndMinistereIdAndStartDateBetween(
            PeriodType periodType, Long ministereId, LocalDateTime start, LocalDateTime end);

    // Update 'ServiceId' to 'serviceId' to match the field name in Statistics
    // entity
    List<Statistics> findByPeriodTypeAndServiceIdAndStartDateBetween(
            PeriodType periodType, Long serviceId, LocalDateTime start, LocalDateTime end);
}