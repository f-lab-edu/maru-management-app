package com.maru.domain.guardian;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.guardian.exception.GuardianErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "guardian")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guardian extends SoftDeletableEntity {

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String name;

    private Boolean isVerified = false;

    private LocalDateTime verifiedAt;

    @Column(length = 500)
    private String pushToken;

    private LocalDateTime pushTokenUpdatedAt;

    private Guardian(String phone, String name) {
        validateInput(phone, name);
        this.phone = phone;
        this.name = name;
        this.isVerified = false;
    }

    public static Guardian create(String phone, String name) {
        return new Guardian(phone, name);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePhone(String phone) {
        DomainAssert.hasText(phone, GuardianErrorCode.PHONE_REQUIRED);
        this.phone = phone;
    }

    public void verify() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public void updatePushToken(String pushToken) {
        this.pushToken = pushToken;
        this.pushTokenUpdatedAt = LocalDateTime.now();
    }

    private void validateInput(String phone, String name) {
        DomainAssert.hasText(phone, GuardianErrorCode.PHONE_REQUIRED);
        DomainAssert.hasText(name, GuardianErrorCode.NAME_REQUIRED);
    }
}
