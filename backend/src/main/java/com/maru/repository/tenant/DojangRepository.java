package com.maru.repository.tenant;

import com.maru.domain.tenant.Dojang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DojangRepository extends JpaRepository<Dojang, String> {

    @Query("""
        SELECT d
        FROM Dojang d
        JOIN FETCH d.owner
        WHERE d.deletedAt IS NULL
        AND d.isActive = true
        """)
    List<Dojang> findAllActiveWithOwner();

    @Query("""
        SELECT d FROM Dojang d
        JOIN FETCH d.owner o
        WHERE d.deletedAt IS NULL
          AND d.isActive = true
          AND (d.name LIKE %:keyword%
               OR d.address LIKE %:keyword%
               OR o.name LIKE %:keyword%)
        """)
    List<Dojang> findByKeywordLike(@Param("keyword") String keyword);

    @Query(value = """
        SELECT d.* FROM dojang d
        JOIN users u ON d.user_id = u.id
        WHERE d.deleted_at IS NULL
          AND d.is_active = true
          AND MATCH(d.name, d.address) AGAINST(:keyword IN BOOLEAN MODE)
        """, nativeQuery = true)
    List<Dojang> findByKeywordFullText(@Param("keyword") String keyword);

    Optional<Dojang> findByOwnerId(String ownerId);
}
