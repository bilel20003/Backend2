package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "produits")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Produit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;
    private String topologie;
    private Double prix;

    @ManyToOne
    @JoinColumn(name = "user_info_id", nullable = false)
    private UserInfo client; // Relation avec UserInfo
}