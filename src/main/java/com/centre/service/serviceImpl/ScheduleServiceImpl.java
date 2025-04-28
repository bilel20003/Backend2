package com.centre.service.serviceImpl;

import com.centre.service.model.Rdv;
import com.centre.service.model.Schedule;
import com.centre.service.repository.RdvRepository;
import com.centre.service.repository.ScheduleRepository;
import com.centre.service.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private RdvRepository rdvRepository;

    @Override
    public Schedule saveSchedule(Schedule schedule) {
        try {
            // Validate schedule
            if (schedule.getStartTime().isAfter(schedule.getEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
            if (schedule.getBreakStart() != null && schedule.getBreakEnd() != null &&
                    (schedule.getBreakStart().isAfter(schedule.getBreakEnd()) ||
                            schedule.getBreakStart().isBefore(schedule.getStartTime()) ||
                            schedule.getBreakEnd().isAfter(schedule.getEndTime()))) {
                throw new IllegalArgumentException("Invalid break time range");
            }

            // Check for overlapping schedules
            List<Schedule> existingSchedules = scheduleRepository.findByDayOfWeek(schedule.getDayOfWeek());
            for (Schedule existing : existingSchedules) {
                if (existing.getId() != null && existing.getId().equals(schedule.getId()))
                    continue; // Skip self
                if (existing.getDayOfWeek().equals(schedule.getDayOfWeek()) &&
                        !(schedule.getEndTime().isBefore(existing.getStartTime()) ||
                                schedule.getStartTime().isAfter(existing.getEndTime()))) {
                    throw new IllegalArgumentException("Schedule overlaps with existing schedule");
                }
            }

            Schedule savedSchedule = scheduleRepository.save(schedule);
            log.info("Schedule saved successfully: {}", savedSchedule);
            return savedSchedule;
        } catch (Exception e) {
            log.error("Error saving schedule: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Schedule> getSchedulesByDay(String dayOfWeek) {
        try {
            return scheduleRepository.findByDayOfWeek(dayOfWeek.toUpperCase());
        } catch (Exception e) {
            log.error("Error retrieving schedules for day {}: {}", dayOfWeek, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<LocalTime> getAvailableSlots(String dayOfWeek, LocalDate date) {
        try {
            // Fetch schedules for the day
            List<Schedule> schedules = scheduleRepository.findByDayOfWeek(dayOfWeek.toUpperCase());

            List<LocalTime> availableSlots = new ArrayList<>();
            for (Schedule schedule : schedules) {
                LocalTime start = schedule.getStartTime();
                LocalTime end = schedule.getEndTime();
                LocalTime breakStart = schedule.getBreakStart();
                LocalTime breakEnd = schedule.getBreakEnd();

                // Generate hourly slots
                for (LocalTime time = start; time.isBefore(end); time = time.plusHours(1)) {
                    // Exclude break times
                    if (breakStart != null && breakEnd != null &&
                            (time.equals(breakStart) || (time.isAfter(breakStart) && time.isBefore(breakEnd)))) {
                        continue;
                    }
                    availableSlots.add(time);
                }
            }

            // Fetch booked slots for the date
            java.sql.Timestamp startOfDay = java.sql.Timestamp.valueOf(date.atStartOfDay());
            java.sql.Timestamp endOfDay = java.sql.Timestamp.valueOf(date.atTime(23, 59, 59));
            List<Rdv> bookedRdvs = rdvRepository.findAll().stream()
                    .filter(rdv -> rdv.getDateSouhaitee().after(startOfDay) && rdv.getDateSouhaitee().before(endOfDay))
                    .collect(Collectors.toList());
            List<LocalTime> bookedTimes = bookedRdvs.stream()
                    .map(rdv -> rdv.getDateSouhaitee().toInstant().atZone(ZoneId.systemDefault())
                            .toLocalTime())
                    .map(time -> time.truncatedTo(java.time.temporal.ChronoUnit.HOURS)) // Round to hour
                    .collect(Collectors.toList());

            // Remove booked slots
            availableSlots.removeIf(time -> bookedTimes.contains(time));

            log.info("Available slots for {}: {}", date, availableSlots);
            return availableSlots;
        } catch (Exception e) {
            log.error("Error retrieving available slots for {}: {}", date, e.getMessage());
            throw e;
        }
    }

    @Override
    public Schedule updateSchedule(Long id, Schedule updatedSchedule) {
        try {
            // Verify schedule exists
            Optional<Schedule> existingScheduleOpt = scheduleRepository.findById(id);
            if (existingScheduleOpt.isEmpty()) {
                log.warn("Tentative de mise à jour d'un schedule qui n'existe pas avec l'ID: {}", id);
                throw new IllegalArgumentException("Schedule non trouvé");
            }

            Schedule existingSchedule = existingScheduleOpt.get();

            // Validate updated schedule
            if (updatedSchedule.getStartTime().isAfter(updatedSchedule.getEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
            if (updatedSchedule.getBreakStart() != null && updatedSchedule.getBreakEnd() != null &&
                    (updatedSchedule.getBreakStart().isAfter(updatedSchedule.getBreakEnd()) ||
                            updatedSchedule.getBreakStart().isBefore(updatedSchedule.getStartTime()) ||
                            updatedSchedule.getBreakEnd().isAfter(updatedSchedule.getEndTime()))) {
                throw new IllegalArgumentException("Invalid break time range");
            }

            // Check for overlapping schedules
            List<Schedule> existingSchedules = scheduleRepository.findByDayOfWeek(updatedSchedule.getDayOfWeek());
            for (Schedule existing : existingSchedules) {
                if (existing.getId().equals(id))
                    continue; // Skip self
                if (existing.getDayOfWeek().equals(updatedSchedule.getDayOfWeek()) &&
                        !(updatedSchedule.getEndTime().isBefore(existing.getStartTime()) ||
                                updatedSchedule.getStartTime().isAfter(existing.getEndTime()))) {
                    throw new IllegalArgumentException("Schedule overlaps with existing schedule");
                }
            }

            // Update fields
            existingSchedule.setDayOfWeek(updatedSchedule.getDayOfWeek());
            existingSchedule.setStartTime(updatedSchedule.getStartTime());
            existingSchedule.setEndTime(updatedSchedule.getEndTime());
            existingSchedule.setBreakStart(updatedSchedule.getBreakStart());
            existingSchedule.setBreakEnd(updatedSchedule.getBreakEnd());

            Schedule savedSchedule = scheduleRepository.save(existingSchedule);
            log.info("Schedule updated successfully: {}", savedSchedule);
            return savedSchedule;
        } catch (Exception e) {
            log.error("Error updating schedule with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteSchedule(Long id) {
        try {
            // Verify schedule exists
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(id);
            if (scheduleOpt.isEmpty()) {
                log.warn("Tentative de suppression d'un schedule qui n'existe pas avec l'ID: {}", id);
                throw new IllegalArgumentException("Schedule non trouvé");
            }

            // Check for associated RDVs
            String dayOfWeek = scheduleOpt.get().getDayOfWeek();
            LocalTime startTime = scheduleOpt.get().getStartTime();
            LocalTime endTime = scheduleOpt.get().getEndTime();
            List<Rdv> rdvs = rdvRepository.findAll().stream()
                    .filter(rdv -> {
                        LocalDate rdvDate = rdv.getDateSouhaitee().toInstant().atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        LocalTime rdvTime = rdv.getDateSouhaitee().toInstant().atZone(ZoneId.systemDefault())
                                .toLocalTime();
                        String rdvDayOfWeek = rdvDate.getDayOfWeek().toString();
                        return rdvDayOfWeek.equals(dayOfWeek) &&
                                !rdvTime.isBefore(startTime) &&
                                !rdvTime.isAfter(endTime);
                    })
                    .collect(Collectors.toList());
            if (!rdvs.isEmpty()) {
                log.warn("Cannot delete schedule with ID {}: associated RDVs exist", id);
                throw new IllegalStateException("Cannot delete schedule with associated RDVs");
            }

            scheduleRepository.deleteById(id);
            log.info("Schedule deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting schedule with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }
}