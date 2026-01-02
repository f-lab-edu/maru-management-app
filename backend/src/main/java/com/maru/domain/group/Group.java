package com.maru.domain.group;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.group.exception.GroupErrorCode;
import com.maru.domain.section.Section;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dojang_id", nullable = false)
    private Dojang dojang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(nullable = false)
    private String name;

    @Column(name = "display_order")
    private Integer displayOrder;

    private Group(Dojang dojang, Section section, String name, Integer displayOrder) {
        this.dojang = dojang;
        this.section = section;
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public static Group create(Dojang dojang, Section section, String name, Integer displayOrder) {
        DomainAssert.notNull(section, GroupErrorCode.SECTION_REQUIRED);
        DomainAssert.hasText(name, GroupErrorCode.NAME_REQUIRED);
        return new Group(dojang, section, name, displayOrder);
    }

    public void updateName(String name) {
        DomainAssert.hasText(name, GroupErrorCode.NAME_REQUIRED);
        this.name = name;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void updateSchedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
