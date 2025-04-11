package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "rdvs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rdv implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSouhaitee;

    private String description;

    private String status;

    private String typeProbleme;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_envoi")
    private Date dateEnvoi;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private UserInfo client; // Le client qui a créé le rendez-vous

    @ManyToOne
    @JoinColumn(name = "guichetier_id")
    private UserInfo guichetier; // Le guichetier qui traite le rendez-vous

}