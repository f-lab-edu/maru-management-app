package com.maru.repository.division;

import com.maru.domain.division.Division;
import com.maru.repository.division.view.DivisionCountBySectionView;
import com.maru.repository.division.view.DivisionView;
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
        WHERE s.dojangId = :dojangId
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
          AND s.dojangId = :dojangId
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
        WHERE d.section.dojangId = :dojangId
        GROUP BY d.section.id
        """)
    List<DivisionCountBySectionView> countDivisionsBySectionForDojang(@Param("dojangId") String dojangId);

    @Query("""
        SELECT d.id as id, d.name as name, d.displayOrder as displayOrder,
               d.scheduleDays as scheduleDays, d.startTime as startTime, d.endTime as endTime,
               s.id as sectionId, s.name as sectionName,
               (SELECT COUNT(e) FROM Enrollment e WHERE e.divisionId = d.id) as studentCount
        FROM Division d
        JOIN d.section s
        WHERE s.dojangId = :dojangId
          AND s.id = :sectionId
        ORDER BY d.displayOrder
        """)
    List<DivisionView> findAllWithStudentCount(
            @Param("dojangId") String dojangId,
            @Param("sectionId") String sectionId);

    @Query("""
        SELECT d.id as id, d.name as name, d.displayOrder as displayOrder,
               d.scheduleDays as scheduleDays, d.startTime as startTime, d.endTime as endTime,
               s.id as sectionId, s.name as sectionName,
               (SELECT COUNT(e) FROM Enrollment e WHERE e.divisionId = d.id) as studentCount
        FROM Division d
        JOIN d.section s
        WHERE d.id = :divisionId
          AND s.dojangId = :dojangId
        """)
    Optional<DivisionView> findDetailById(
            @Param("divisionId") String divisionId,
            @Param("dojangId") String dojangId);
}
