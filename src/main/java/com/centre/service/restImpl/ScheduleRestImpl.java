package com.centre.service.restImpl;

import com.centre.service.model.Schedule;
import com.centre.service.rest.ScheduleRest;
import com.centre.service.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
public class ScheduleRestImpl implements ScheduleRest {
    private static final Logger log = LoggerFactory.getLogger(ScheduleRestImpl.class);

    @Autowired
    private ScheduleService scheduleService;

    @Override
    public ResponseEntity<?> addSchedule(Schedule schedule) {
        try {
            Schedule savedSchedule = scheduleService.saveSchedule(schedule);
            return new ResponseEntity<>(savedSchedule, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding schedule: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error adding schedule: " + e.getMessage() + "\"}",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> getSchedulesByDay(String dayOfWeek) {
        try {
            List<Schedule> schedules = scheduleService.getSchedulesByDay(dayOfWeek);
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving schedules for day {}: {}", dayOfWeek, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error retrieving schedules\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAvailableSlots(String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            String dayOfWeek = localDate.getDayOfWeek().toString();
            List<LocalTime> slots = scheduleService.getAvailableSlots(dayOfWeek, localDate);
            return new ResponseEntity<>(slots, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving available slots for date {}: {}", date, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error retrieving available slots\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateSchedule(Long id, Schedule schedule) {
        try {
            Schedule updatedSchedule = scheduleService.updateSchedule(id, schedule);
            return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating schedule with ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error updating schedule: " + e.getMessage() + "\"}",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> deleteSchedule(Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return new ResponseEntity<>("{\"message\":\"Schedule deleted successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error deleting schedule with ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error deleting schedule: " + e.getMessage() + "\"}",
                    HttpStatus.BAD_REQUEST);
        }
    }
}