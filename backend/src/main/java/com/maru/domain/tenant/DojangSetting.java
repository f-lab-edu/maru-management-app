package com.maru.domain.tenant;

import com.maru.domain.common.BaseEntity;
import com.maru.domain.tenant.exception.DojangSettingErrorCode;
import com.maru.common.exception.DomainAssert;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dojang_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DojangSetting extends BaseEntity {

    @Column(name = "dojang_id", nullable = false, unique = true, length = 13)
    private String dojangId;

    @Column(name = "default_tuition")
    private Integer defaultTuition;

    @Column(name = "auto_invoice_enabled", nullable = false)
    private Boolean autoInvoiceEnabled = false;

    @Column(name = "auto_invoice_day")
    private Integer autoInvoiceDay;

    @Column(name = "auto_invoice_hour", nullable = false)
    private Integer autoInvoiceHour = 9;

    @Column(name = "auto_absence_hour", nullable = false)
    private Integer autoAbsenceHour = 9;

    private DojangSetting(String dojangId) {
        DomainAssert.hasText(dojangId, DojangSettingErrorCode.DOJANG_REQUIRED);
        this.dojangId = dojangId;
        this.autoInvoiceEnabled = false;
    }

    public static DojangSetting create(String dojangId) {
        return new DojangSetting(dojangId);
    }

    public void updateTuition(Integer defaultTuition) {
        this.defaultTuition = defaultTuition;
    }

    public void updateAutoInvoice(Boolean autoInvoiceEnabled, Integer autoInvoiceDay, Integer autoInvoiceHour) {
        this.autoInvoiceEnabled = autoInvoiceEnabled != null ? autoInvoiceEnabled : false;
        this.autoInvoiceDay = autoInvoiceDay;
        this.autoInvoiceHour = autoInvoiceHour != null ? autoInvoiceHour : 9;
    }

    public void updateAutoAbsenceHour(Integer autoAbsenceHour) {
        this.autoAbsenceHour = autoAbsenceHour != null ? autoAbsenceHour : 9;
    }
}
