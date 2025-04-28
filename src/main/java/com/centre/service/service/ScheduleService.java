package com.centre.service.service;

import com.centre.service.model.Schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleService {
    Schedule saveSchedule(Schedule schedule);

    List<Schedule> getSchedulesByDay(String dayOfWeek);

    List<LocalTime> getAvailableSlots(String dayOfWeek, LocalDate date);

    Schedule updateSchedule(Long id, Schedule schedule);

    void deleteSchedule(Long id);
}