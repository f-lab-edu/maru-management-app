package com.maru.repository.guardian;

import com.maru.domain.guardian.Guardianship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardianshipRepository extends JpaRepository<Guardianship, Long> {

    List<Guardianship> findByStudentIdAndDeletedAtIsNull(Long studentId);

    Optional<Guardianship> findByStudentIdAndIsPrimaryTrueAndDeletedAtIsNull(Long studentId);

    Optional<Guardianship> findByStudentIdAndGuardianIdAndDeletedAtIsNull(Long studentId, Long guardianId);
}
