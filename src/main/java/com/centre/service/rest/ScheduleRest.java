package com.centre.service.rest;

import com.centre.service.model.Schedule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/api/schedules")
public interface ScheduleRest {
    @PostMapping
    ResponseEntity<?> addSchedule(@RequestBody Schedule schedule);

    @GetMapping("/{dayOfWeek}")
    ResponseEntity<?> getSchedulesByDay(@PathVariable String dayOfWeek);

    @GetMapping("/available-slots")
    ResponseEntity<?> getAvailableSlots(@RequestParam String date);

    @PutMapping("/{id}")
    ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule);

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteSchedule(@PathVariable Long id);
}