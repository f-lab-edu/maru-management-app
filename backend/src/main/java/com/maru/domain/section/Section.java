package com.maru.domain.section;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.section.exception.SectionErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Section extends BaseEntity {

    @Column(name = "dojang_id", nullable = false)
    private String dojangId;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "display_order")
    private Integer displayOrder;

    private Section(String dojangId, String name, Integer displayOrder) {
        this.dojangId = dojangId;
        this.name = name;
        this.isActive = true;
        this.displayOrder = displayOrder;
    }

    public static Section create(String dojangId, String name, Integer displayOrder) {
        DomainAssert.hasText(dojangId, SectionErrorCode.DOJANG_REQUIRED);
        DomainAssert.hasText(name, SectionErrorCode.NAME_REQUIRED);
        return new Section(dojangId, name, displayOrder);
    }

    public void updateName(String name) {
        DomainAssert.hasText(name, SectionErrorCode.NAME_REQUIRED);
        this.name = name;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
