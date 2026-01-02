package com.maru.repository.group;

import com.maru.domain.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, String> {

    @Query("""
        SELECT g FROM Group g
        WHERE g.section.id = :sectionId
          AND g.deletedAt IS NULL
        ORDER BY g.displayOrder
        """)
    List<Group> findAllBySectionIdOrderByDisplayOrder(@Param("sectionId") String sectionId);

    @Query("""
        SELECT g FROM Group g
        JOIN FETCH g.section s
        WHERE s.dojang.id = :dojangId
          AND g.deletedAt IS NULL
        ORDER BY s.displayOrder, g.displayOrder
        """)
    List<Group> findAllWithSectionByDojangId(@Param("dojangId") String dojangId);

    @Query("""
        SELECT g FROM Group g
        JOIN FETCH g.section s
        WHERE s.dojang.id = :dojangId
          AND s.id = :sectionId
          AND g.deletedAt IS NULL
        ORDER BY g.displayOrder
        """)
    List<Group> findAllWithSectionByDojangIdAndSectionId(
            @Param("dojangId") String dojangId,
            @Param("sectionId") String sectionId);

    @Query("""
        SELECT g FROM Group g
        JOIN FETCH g.section s
        WHERE g.id = :groupId
          AND s.dojang.id = :dojangId
          AND g.deletedAt IS NULL
        """)
    Optional<Group> findByIdAndDojangIdWithSection(
            @Param("groupId") String groupId,
            @Param("dojangId") String dojangId);

    @Query("""
        SELECT COUNT(g) > 0 FROM Group g
        WHERE g.section.id = :sectionId
          AND g.name = :name
          AND g.deletedAt IS NULL
        """)
    boolean existsBySectionIdAndName(
            @Param("sectionId") String sectionId,
            @Param("name") String name);

    @Query("""
        SELECT COUNT(g) > 0 FROM Group g
        WHERE g.section.id = :sectionId
          AND g.name = :name
          AND g.id != :id
          AND g.deletedAt IS NULL
        """)
    boolean existsBySectionIdAndNameAndIdNot(
            @Param("sectionId") String sectionId,
            @Param("name") String name,
            @Param("id") String id);

    @Query("""
        SELECT COUNT(g) > 0 FROM Group g
        WHERE g.section.id = :sectionId
          AND g.deletedAt IS NULL
        """)
    boolean existsBySectionId(@Param("sectionId") String sectionId);

    @Query("""
        SELECT COALESCE(MAX(g.displayOrder), -1)
        FROM Group g
        WHERE g.section.id = :sectionId
          AND g.deletedAt IS NULL
        """)
    int findMaxDisplayOrderBySectionId(@Param("sectionId") String sectionId);
}
