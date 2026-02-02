package com.maru.repository.tenant;

import com.maru.domain.tenant.DojangSetting;
import com.maru.repository.tenant.view.AutoAbsenceTargetView;
import com.maru.repository.tenant.view.AutoInvoiceTargetView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DojangSettingRepository extends JpaRepository<DojangSetting, String> {

    Optional<DojangSetting> findByDojangId(String dojangId);

    @Query("""
        SELECT ds.dojangId as dojangId, d.tenantId as tenantId, ds.defaultTuition as defaultTuition
        FROM DojangSetting ds
        JOIN Dojang d ON ds.dojangId = d.id
        WHERE ds.autoInvoiceEnabled = true
          AND ds.defaultTuition IS NOT NULL
          AND ds.autoInvoiceHour = :currentHour
          AND d.isActive = true
          AND d.deletedAt IS NULL
          AND (ds.autoInvoiceDay = :dayOfMonth
               OR (:isLastDay = true AND ds.autoInvoiceDay > :dayOfMonth))
        """)
    List<AutoInvoiceTargetView> findAllAutoInvoiceTargets(
            @Param("dayOfMonth") int dayOfMonth,
            @Param("isLastDay") boolean isLastDay,
            @Param("currentHour") int currentHour);

    @Query("""
        SELECT ds.dojangId as dojangId, d.tenantId as tenantId
        FROM DojangSetting ds
        JOIN Dojang d ON ds.dojangId = d.id
        WHERE ds.autoAbsenceHour = :currentHour
          AND d.isActive = true
          AND d.deletedAt IS NULL
        """)
    List<AutoAbsenceTargetView> findAllAutoAbsenceTargets(@Param("currentHour") int currentHour);
}
