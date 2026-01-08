package com.maru.domain.employment;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseTimeEntity;
import com.maru.domain.employment.exception.EmploymentErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employment_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmploymentHistory extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_id", nullable = false)
    private Employment employment;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EmploymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentStatus toStatus;

    @Column(name = "user_id")
    private String changedBy;

    @Column(length = 500)
    private String reason;

    private EmploymentHistory(Employment employment, EmploymentStatus fromStatus, EmploymentStatus toStatus, String changedBy, String reason) {
        validateNotNull(employment, toStatus);
        this.employment = employment;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.reason = reason;
    }

    public static EmploymentHistory record(Employment employment, EmploymentStatus fromStatus, EmploymentStatus toStatus, String changedBy, String reason) {
        return new EmploymentHistory(employment, fromStatus, toStatus, changedBy, reason);
    }

    private void validateNotNull(Employment employment, EmploymentStatus toStatus) {
        DomainAssert.notNull(employment, EmploymentErrorCode.EMPLOYMENT_REQUIRED);
        DomainAssert.notNull(toStatus, EmploymentErrorCode.STATUS_REQUIRED);
    }
}
