package com.centre.service.service;

import com.centre.service.model.*;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.RdvRepository;
import com.centre.service.repository.StatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;

@Service
public class StatisticsService {

    private final RequeteRepository requeteRepository;
    private final RdvRepository rdvRepository;
    private final StatisticsRepository statisticsRepository;

    @Autowired
    public StatisticsService(RequeteRepository requeteRepository, RdvRepository rdvRepository,
            StatisticsRepository statisticsRepository) {
        this.requeteRepository = requeteRepository;
        this.rdvRepository = rdvRepository;
        this.statisticsRepository = statisticsRepository;
    }

    // Existing methods (aggregateAndSaveStatistics, etc.) remain unchanged
    @Transactional
    public void aggregateAndSaveStatistics() {
        LocalDateTime now = LocalDateTime.now();
        aggregateDailyStatistics(now);
        aggregateWeeklyStatistics(now);
        aggregateMonthlyStatistics(now);
        aggregateThreeMonthsStatistics(now);
        aggregateSixMonthsStatistics(now);
        aggregateYearlyStatistics(now);
    }

    private void aggregateDailyStatistics(LocalDateTime now) {
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        if (now.isAfter(endOfDay)) {
            saveStatisticsForPeriod(PeriodType.DAY, startOfDay, endOfDay);
        }
    }

    private void aggregateWeeklyStatistics(LocalDateTime now) {
        LocalDateTime startOfWeek = now.toLocalDate().atStartOfDay()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime endOfWeek = startOfWeek.plusWeeks(1).minusSeconds(1);
        if (now.isAfter(endOfWeek)) {
            saveStatisticsForPeriod(PeriodType.WEEK, startOfWeek, endOfWeek);
        }
    }

    private void aggregateMonthlyStatistics(LocalDateTime now) {
        LocalDateTime startOfMonth = now.toLocalDate().atStartOfDay().withDayOfMonth(1);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        if (now.isAfter(endOfMonth)) {
            saveStatisticsForPeriod(PeriodType.MONTH, startOfMonth, endOfMonth);
        }
    }

    private void aggregateThreeMonthsStatistics(LocalDateTime now) {
        LocalDateTime startOfPeriod = now.toLocalDate().atStartOfDay().withDayOfMonth(1).minusMonths(2);
        LocalDateTime endOfPeriod = startOfPeriod.plusMonths(3).minusSeconds(1);
        if (now.isAfter(endOfPeriod)) {
            saveStatisticsForPeriod(PeriodType.THREE_MONTHS, startOfPeriod, endOfPeriod);
        }
    }

    private void aggregateSixMonthsStatistics(LocalDateTime now) {
        LocalDateTime startOfPeriod = now.toLocalDate().atStartOfDay().withDayOfMonth(1).minusMonths(5);
        LocalDateTime endOfPeriod = startOfPeriod.plusMonths(6).minusSeconds(1);
        if (now.isAfter(endOfPeriod)) {
            saveStatisticsForPeriod(PeriodType.SIX_MONTHS, startOfPeriod, endOfPeriod);
        }
    }

    private void aggregateYearlyStatistics(LocalDateTime now) {
        LocalDateTime startOfYear = now.toLocalDate().atStartOfDay().withDayOfYear(1);
        LocalDateTime endOfYear = startOfYear.plusYears(1).minusSeconds(1);
        if (now.isAfter(endOfYear)) {
            saveStatisticsForPeriod(PeriodType.YEAR, startOfYear, endOfYear);
        }
    }

    private void saveStatisticsForPeriod(PeriodType periodType, LocalDateTime start, LocalDateTime end) {
        List<Requete> requests = requeteRepository.findByDateBetweenAndArchiverFalse(
                Date.from(start.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));

        List<Rdv> rdvs = rdvRepository.findByDateEnvoiBetweenAndArchiverFalse(
                Date.from(start.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));

        Statistics stats = new Statistics();
        stats.setPeriodType(periodType);
        stats.setStartDate(start);
        stats.setEndDate(end);

        stats.setTotalRequests((long) requests.size());
        stats.setNewRequests(requests.stream().filter(r -> r.getEtat() == EtatRequete.NOUVEAU).count());
        stats.setDraftRequests(requests.stream().filter(r -> r.getEtat() == EtatRequete.BROUILLON).count());
        stats.setInProgressRequests(
                requests.stream().filter(r -> r.getEtat() == EtatRequete.EN_COURS_DE_TRAITEMENT).count());
        stats.setProcessedRequests(requests.stream().filter(r -> r.getEtat() == EtatRequete.TRAITEE).count());
        stats.setRefusedRequests(requests.stream().filter(r -> r.getEtat() == EtatRequete.REFUSEE).count());

        double avgProcessingTime = requests.stream()
                .filter(r -> r.getEtat() == EtatRequete.TRAITEE && r.getDate() != null && r.getDateTraitement() != null)
                .mapToLong(r -> Duration.between(
                        r.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        r.getDateTraitement().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()).toMinutes())
                .average()
                .orElse(0.0);
        stats.setAvgProcessingTimeMinutes(avgProcessingTime);

        stats.setTotalRdvs((long) rdvs.size());
        stats.setPendingRdvs(rdvs.stream().filter(r -> "EN_ATTENTE".equalsIgnoreCase(r.getStatus())).count());
        stats.setCompletedRdvs(rdvs.stream().filter(r -> "TERMINE".equalsIgnoreCase(r.getStatus())).count());
        stats.setRefusedRdvs(rdvs.stream().filter(r -> "REFUSE".equalsIgnoreCase(r.getStatus())).count());

        statisticsRepository.save(stats);
    }

    public List<Statistics> getStatisticsForComparison(PeriodType periodType, LocalDateTime start, LocalDateTime end) {
        return statisticsRepository.findByPeriodTypeAndStartDateBetween(periodType, start, end);
    }
}