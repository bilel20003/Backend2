package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity 
@Table(name = "user_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedQuery(name = "User  Info.getAllAppuser", query = "SELECT new com.centre.service.model.UserInfo(u.id, u.name, u.email, u.status, u.role) FROM UserInfo u WHERE u.isDeletable = 'true' AND u.email NOT IN (:email)")
public class UserInfo implements Serializable {

    private static final long SerialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    private String password;
    private String isDeletable;
    private String status;
    
    @Enumerated(EnumType.STRING)
    private Role role;

    



    // Getters et Setters

    public UserInfo(Long id, String name, String email, String status, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.role = role;
    }

    

    
}