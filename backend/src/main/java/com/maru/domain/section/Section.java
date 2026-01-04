package com.maru.domain.section;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.section.exception.SectionErrorCode;
import com.maru.domain.tenant.Dojang;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Section extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dojang_id", nullable = false)
    private Dojang dojang;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "display_order")
    private Integer displayOrder;

    private Section(Dojang dojang, String name, Integer displayOrder) {
        this.dojang = dojang;
        this.name = name;
        this.isActive = true;
        this.displayOrder = displayOrder;
    }

    public static Section create(Dojang dojang, String name, Integer displayOrder) {
        DomainAssert.notNull(dojang, SectionErrorCode.DOJANG_REQUIRED);
        DomainAssert.hasText(name, SectionErrorCode.NAME_REQUIRED);
        return new Section(dojang, name, displayOrder);
    }

    public void updateName(String name) {
        DomainAssert.hasText(name, SectionErrorCode.NAME_REQUIRED);
        this.name = name;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
