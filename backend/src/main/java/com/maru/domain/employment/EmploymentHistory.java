package com.maru.domain.employment;

import com.maru.domain.common.BaseEntity;
import com.maru.domain.common.BaseTimeEntity;
import com.maru.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(
    name = "employment_history",
    indexes = {
        @Index(name = "idx_employment_history_employment_created", columnList = "employment_id, created_at"),
        @Index(name = "idx_employment_history_to_status_created", columnList = "to_status, created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmploymentHistory extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_id", nullable = false)
    private Employment employment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private EmploymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private EmploymentStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User changedBy;

    @Column(length = 500)
    private String reason;

    private EmploymentHistory(Employment employment, EmploymentStatus fromStatus, EmploymentStatus toStatus, User changedBy, String reason) {
        validateNotNull(employment, toStatus);
        this.employment = employment;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.reason = reason;
    }

    public static EmploymentHistory record(Employment employment, EmploymentStatus fromStatus, EmploymentStatus toStatus, User changedBy, String reason) {
        return new EmploymentHistory(employment, fromStatus, toStatus, changedBy, reason);
    }

    private void validateNotNull(Employment employment, EmploymentStatus toStatus) {
        Assert.notNull(employment, "employment는 필수입니다.");
        Assert.notNull(toStatus, "toStatus는 필수입니다.");
    }
}
