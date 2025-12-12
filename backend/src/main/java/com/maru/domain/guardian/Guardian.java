package com.maru.domain.guardian;

import com.maru.domain.common.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "guardian",
    indexes = {
        @Index(name = "idx_guardian_phone", columnList = "phone"),
        @Index(name = "idx_guardian_verified", columnList = "is_verified")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guardian extends SoftDeletableEntity {

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column
    private LocalDateTime verifiedAt;

    @Column(length = 500)
    private String pushToken;

    @Column
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
        Assert.hasText(phone, "phone은 필수입니다.");
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
        Assert.hasText(phone, "phone은 필수입니다.");
        Assert.hasText(name, "name은 필수입니다.");
    }
}
