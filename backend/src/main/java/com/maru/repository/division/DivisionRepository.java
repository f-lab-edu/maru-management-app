package com.maru.repository.division;

import com.maru.domain.division.Division;
import com.maru.repository.division.projection.DivisionCountBySection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DivisionRepository extends JpaRepository<Division, String> {

    @Query("""
        SELECT d FROM Division d
        WHERE d.section.id = :sectionId
          AND d.deletedAt IS NULL
        ORDER BY d.displayOrder
        """)
    List<Division> findAllBySectionIdOrderByDisplayOrder(@Param("sectionId") String sectionId);

    @Query("""
        SELECT d FROM Division d
        JOIN FETCH d.section s
        WHERE s.dojang.id = :dojangId
          AND d.deletedAt IS NULL
        ORDER BY s.displayOrder, d.displayOrder
        """)
    List<Division> findAllWithSectionByDojangId(@Param("dojangId") String dojangId);

    @Query("""
        SELECT d FROM Division d
        JOIN FETCH d.section s
        WHERE s.dojang.id = :dojangId
          AND s.id = :sectionId
          AND d.deletedAt IS NULL
        ORDER BY d.displayOrder
        """)
    List<Division> findAllWithSectionByDojangIdAndSectionId(
            @Param("dojangId") String dojangId,
            @Param("sectionId") String sectionId);

    @Query("""
        SELECT d FROM Division d
        JOIN FETCH d.section s
        WHERE d.id = :divisionId
          AND s.dojang.id = :dojangId
          AND d.deletedAt IS NULL
        """)
    Optional<Division> findByIdAndDojangIdWithSection(
            @Param("divisionId") String divisionId,
            @Param("dojangId") String dojangId);

    @Query("""
        SELECT COUNT(d) > 0 FROM Division d
        WHERE d.section.id = :sectionId
          AND d.name = :name
          AND d.deletedAt IS NULL
        """)
    boolean existsBySectionIdAndName(
            @Param("sectionId") String sectionId,
            @Param("name") String name);

    @Query("""
        SELECT COUNT(d) > 0 FROM Division d
        WHERE d.section.id = :sectionId
          AND d.name = :name
          AND d.id != :id
          AND d.deletedAt IS NULL
        """)
    boolean existsBySectionIdAndNameAndIdNot(
            @Param("sectionId") String sectionId,
            @Param("name") String name,
            @Param("id") String id);

    @Query("""
        SELECT COUNT(d) > 0 FROM Division d
        WHERE d.section.id = :sectionId
          AND d.deletedAt IS NULL
        """)
    boolean existsBySectionId(@Param("sectionId") String sectionId);

    @Query("""
        SELECT COALESCE(MAX(d.displayOrder), -1)
        FROM Division d
        WHERE d.section.id = :sectionId
          AND d.deletedAt IS NULL
        """)
    int findMaxDisplayOrderBySectionId(@Param("sectionId") String sectionId);

    @Query("""
        SELECT d.section.id AS sectionId, COUNT(d) AS divisionCount
        FROM Division d
        WHERE d.section.dojang.id = :dojangId
          AND d.deletedAt IS NULL
        GROUP BY d.section.id
        """)
    List<DivisionCountBySection> countDivisionsBySectionForDojang(@Param("dojangId") String dojangId);
}
