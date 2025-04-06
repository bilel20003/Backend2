package com.centre.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.centre.service.model.UserInfo; 
import com.centre.service.model.Role;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    
    boolean existsByEmail(String email);

    Optional<UserInfo> findByEmail(String email); // Ajout pour l'authentification

    List<UserInfo> findByRole(Role role);

    List<UserInfo> findByNameContainingIgnoreCase(String name); // Modifié pour correspondre à 'name'
    
    List<UserInfo> findAllByRole(Role role);

}