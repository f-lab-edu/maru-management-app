package com.maru.repository.section;

import com.maru.domain.section.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, String> {

    @Query("""
        SELECT s FROM Section s
        WHERE s.dojang.id = :dojangId
          AND s.deletedAt IS NULL
        ORDER BY s.displayOrder
        """)
    List<Section> findAllByDojangIdOrderByDisplayOrder(@Param("dojangId") String dojangId);

    @Query("""
        SELECT COUNT(s) > 0 FROM Section s
        WHERE s.dojang.id = :dojangId
          AND s.name = :name
          AND s.deletedAt IS NULL
        """)
    boolean existsByDojangIdAndName(@Param("dojangId") String dojangId, @Param("name") String name);

    @Query("""
        SELECT COUNT(s) > 0 FROM Section s
        WHERE s.dojang.id = :dojangId
          AND s.name = :name
          AND s.id != :id
          AND s.deletedAt IS NULL
        """)
    boolean existsByDojangIdAndNameAndIdNot(
            @Param("dojangId") String dojangId,
            @Param("name") String name,
            @Param("id") String id);

    @Query("""
        SELECT s FROM Section s
        WHERE s.id = :id
          AND s.dojang.id = :dojangId
          AND s.deletedAt IS NULL
        """)
    Optional<Section> findByIdAndDojangId(@Param("id") String id, @Param("dojangId") String dojangId);

    @Query("""
        SELECT COALESCE(MAX(s.displayOrder), -1)
        FROM Section s
        WHERE s.dojang.id = :dojangId
          AND s.deletedAt IS NULL
        """)
    int findMaxDisplayOrderByDojangId(@Param("dojangId") String dojangId);
}
