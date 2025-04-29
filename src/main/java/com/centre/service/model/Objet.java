package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "objets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Objet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit; // Relation avec le produit

    @Column(nullable = false)
    private boolean archiver = false;
}
