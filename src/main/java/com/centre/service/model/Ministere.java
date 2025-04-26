package com.centre.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "ministere")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ministere implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomMinistere;

    @JsonIgnore
    @OneToMany(mappedBy = "ministere", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Servicee> services;
}