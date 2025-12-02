package com.maru.repository.tenant;

import com.maru.domain.tenant.Dojang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DojangRepository extends JpaRepository<Dojang, Long> {

    @Query("SELECT d FROM Dojang d JOIN FETCH d.owner WHERE d.deletedAt IS NULL AND d.isActive = true")
    List<Dojang> findAllActiveWithOwner();

    @Query("SELECT d FROM Dojang d JOIN FETCH d.owner o WHERE d.deletedAt IS NULL AND d.isActive = true " +
           "AND (d.name LIKE %:keyword% OR d.address LIKE %:keyword% OR o.name LIKE %:keyword%)")
    List<Dojang> findByKeywordLike(@Param("keyword") String keyword);
}
