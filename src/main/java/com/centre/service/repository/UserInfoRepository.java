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

    Optional<UserInfo> findByEmailAndArchiverFalse(String email);

    @Query("SELECT new com.centre.service.model.UserInfo(u.id, u.name, u.email, u.status, u.role, u.service, u.produit) "
            +
            "FROM UserInfo u WHERE u.isDeletable = 'true' AND u.email NOT IN (:email) AND u.archiver = false " +
            "AND u.service.archiver = false AND u.produit.archiver = false")
    List<UserInfo> getAllAppuser(@Param("email") String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserInfo u WHERE u.role.name = 'GUICHETIER' AND u.status = 'true' AND u.archiver = false")
    List<UserInfo> findActiveGuichetiers();

    @Query("SELECT u FROM UserInfo u WHERE u.role.name = 'TECHNICIEN' AND u.status = 'true' AND u.archiver = false")
    List<UserInfo> findActiveTechniciens();

    Optional<UserInfo> findByIdAndArchiverFalse(Long id);

    @Query("SELECT COUNT(u) FROM UserInfo u WHERE u.produit.id = :produitId AND u.role.name != 'CLIENT' AND u.archiver = false")
    long countNonClientUsersByProduitId(@Param("produitId") Long produitId);

    @Query("SELECT u FROM UserInfo u WHERE u.role.name = :roleName AND u.archiver = false")
    List<UserInfo> findByRoleNameAndArchiverFalse(@Param("roleName") String roleName);

    List<UserInfo> findByArchiverTrue();

    Optional<UserInfo> findById(Long id);

    Optional<UserInfo> findByResetToken(String resetToken);
}