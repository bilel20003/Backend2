package com.centre.service.repository;

import com.centre.service.model.Ministere;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MinistereRepository extends JpaRepository<Ministere, Long> {
    Optional<Ministere> findByNomMinistereAndArchiverFalse(String nomMinistere);

    Optional<Ministere> findByIdAndArchiverFalse(Long id);

    List<Ministere> findByArchiverFalse();

    List<Ministere> findByArchiverTrue();

    Optional<Ministere> findById(Long id);
}