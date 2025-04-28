package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schedule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek; // e.g., "MONDAY", "TUESDAY"

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // e.g., 08:00

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // e.g., 17:00

    @Column(name = "break_start")
    private LocalTime breakStart; // e.g., 12:00

    @Column(name = "break_end")
    private LocalTime breakEnd; // e.g., 13:00
}