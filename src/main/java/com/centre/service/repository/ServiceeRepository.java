package com.centre.service.repository;

import com.centre.service.model.Servicee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceeRepository extends JpaRepository<Servicee, Long> {
    Optional<Servicee> findByNomServiceAndArchiverFalse(String nomService);

    @Query("SELECT s FROM Servicee s JOIN FETCH s.ministere m WHERE s.archiver = false AND m.archiver = false")
    List<Servicee> findAllWithMinistere();

    Optional<Servicee> findByIdAndArchiverFalse(Long id);

    List<Servicee> findByArchiverTrue();

    Optional<Servicee> findById(Long id);
}