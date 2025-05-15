package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "piece_jointe")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PieceJointe implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomFichier;
    private String typeFichier;
    private String cheminFichier;
    private Date dateUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requete_id", nullable = false)
    @JsonBackReference
    private Requete requete;

    @Column(nullable = false)
    private boolean archiver = false;
}