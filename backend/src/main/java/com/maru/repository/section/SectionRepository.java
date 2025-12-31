package com.maru.repository.section;

import com.maru.domain.section.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, String> {

    List<Section> findAllByDojangIdOrderByDisplayOrder(String dojangId);

    boolean existsByDojangIdAndName(String dojangId, String name);

    boolean existsByDojangIdAndNameAndIdNot(String dojangId, String name, String id);

    Optional<Section> findByIdAndDojangId(String id, String dojangId);

    @Query("""
        SELECT COALESCE(MAX(s.displayOrder), -1)
        FROM Section s
        WHERE s.dojang.id = :dojangId
          AND s.deletedAt IS NULL
        """)
    Optional<Integer> findMaxDisplayOrderByDojangId(@Param("dojangId") String dojangId);
}
