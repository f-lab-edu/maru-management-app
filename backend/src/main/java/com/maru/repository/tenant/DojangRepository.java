package com.maru.repository.tenant;

import com.maru.domain.tenant.Dojang;
import com.maru.repository.tenant.view.DojangSearchView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DojangRepository extends JpaRepository<Dojang, String> {

    @Query("""
        SELECT d.id as id, d.name as name, d.address as address, u.name as ownerName
        FROM Dojang d JOIN User u ON d.ownerId = u.id
        WHERE d.deletedAt IS NULL
        AND d.isActive = true
        """)
    List<DojangSearchView> findAllActiveForSearch();

    @Query("""
        SELECT d.id as id, d.name as name, d.address as address, u.name as ownerName
        FROM Dojang d JOIN User u ON d.ownerId = u.id
        WHERE d.deletedAt IS NULL
          AND d.isActive = true
          AND (d.name LIKE %:keyword%
               OR d.address LIKE %:keyword%
               OR u.name LIKE %:keyword%)
        """)
    List<DojangSearchView> findByKeywordLike(@Param("keyword") String keyword);

    @Query(value = """
        SELECT d.id as id, d.name as name, d.address as address, u.name as ownerName
        FROM dojang d
        JOIN users u ON d.user_id = u.id
        WHERE d.deleted_at IS NULL
          AND d.is_active = true
          AND MATCH(d.name, d.address) AGAINST(:keyword IN BOOLEAN MODE)
        """, nativeQuery = true)
    List<DojangSearchView> findByKeywordFullText(@Param("keyword") String keyword);

    Optional<Dojang> findByOwnerId(String ownerId);

    @Query("SELECT d.id FROM Dojang d WHERE d.ownerId = :ownerId AND d.deletedAt IS NULL")
    Optional<String> findIdByOwnerId(@Param("ownerId") String ownerId);
}
