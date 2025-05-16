package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "statistics")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType periodType;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Long totalRequests = 0L;

    @Column(nullable = false)
    private Long processedRequests = 0L;

    @Column(nullable = false)
    private Long refusedRequests = 0L;

    @Column(nullable = false)
    private Long inProgressRequests = 0L;

    @Column(nullable = false)
    private Long newRequests = 0L;

    @Column(nullable = false)
    private Long draftRequests = 0L;

    @Column(nullable = false)
    private Double avgProcessingTimeMinutes = 0.0;

    @Column(nullable = false)
    private Long totalRdvs = 0L;

    @Column(nullable = false)
    private Long completedRdvs = 0L;

    @Column(nullable = false)
    private Long refusedRdvs = 0L;

    @Column(nullable = false)
    private Long pendingRdvs = 0L;

    @ManyToOne
    @JoinColumn(name = "ministere_id")
    private Ministere ministere;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Servicee service;
}