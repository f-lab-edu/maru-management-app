package com.maru.repository.division;

import com.maru.domain.division.Division;
import com.maru.repository.division.view.DivisionCountBySectionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DivisionRepository extends JpaRepository<Division, String> {

    @Query("""
        SELECT d FROM Division d
        WHERE d.section.id = :sectionId
        ORDER BY d.displayOrder
        """)
    List<Division> findAllBySectionIdOrderByDisplayOrder(@Param("sectionId") String sectionId);

    @Query("""
        SELECT d FROM Division d
        JOIN FETCH d.section s
        WHERE s.dojang.id = :dojangId
          AND s.id = :sectionId
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
        """)
    Optional<Division> findByIdAndDojangIdWithSection(
            @Param("divisionId") String divisionId,
            @Param("dojangId") String dojangId);

    @Query("""
        SELECT COUNT(d) > 0 FROM Division d
        WHERE d.section.id = :sectionId
          AND d.name = :name
        """)
    boolean existsBySectionIdAndName(
            @Param("sectionId") String sectionId,
            @Param("name") String name);

    @Query("""
        SELECT COUNT(d) > 0 FROM Division d
        WHERE d.section.id = :sectionId
          AND d.name = :name
          AND d.id != :id
        """)
    boolean existsBySectionIdAndNameAndIdNot(
            @Param("sectionId") String sectionId,
            @Param("name") String name,
            @Param("id") String id);

    @Query("""
        SELECT COUNT(d) > 0 FROM Division d
        WHERE d.section.id = :sectionId
        """)
    boolean existsBySectionId(@Param("sectionId") String sectionId);

    @Query("""
        SELECT COALESCE(MAX(d.displayOrder), -1)
        FROM Division d
        WHERE d.section.id = :sectionId
        """)
    int findMaxDisplayOrderBySectionId(@Param("sectionId") String sectionId);

    @Query("""
        SELECT d.section.id AS sectionId, COUNT(d) AS divisionCount
        FROM Division d
        WHERE d.section.dojang.id = :dojangId
        GROUP BY d.section.id
        """)
    List<DivisionCountBySectionView> countDivisionsBySectionForDojang(@Param("dojangId") String dojangId);
}
