package com.centre.service.repository;

import com.centre.service.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    
    Optional<UserInfo> findByEmail(String email); // Ajout pour l'authentification
    
    @Query(name = "User  Info.getAllAppuser")
    List<UserInfo> getAllAppuser(@Param("email") String email);
    
    boolean existsByEmail(String email);

}