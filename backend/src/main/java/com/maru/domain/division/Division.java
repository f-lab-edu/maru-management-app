package com.maru.domain.division;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.division.exception.DivisionErrorCode;
import com.maru.domain.section.Section;
import com.maru.domain.tenant.Dojang;
import com.maru.common.converter.ScheduleDaysConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.util.Set;

@Entity
@Table(name = "division")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Division extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dojang_id", nullable = false)
    private Dojang dojang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @Convert(converter = ScheduleDaysConverter.class)
    @Column(name = "schedule_days", columnDefinition = "TINYINT")
    private Set<DayOfWeek> scheduleDays;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(nullable = false)
    private String name;

    @Column(name = "display_order")
    private Integer displayOrder;

    private Division(Dojang dojang, Section section, String name, Integer displayOrder) {
        this.dojang = dojang;
        this.section = section;
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public static Division create(Dojang dojang, Section section, String name, Integer displayOrder) {
        DomainAssert.notNull(section, DivisionErrorCode.SECTION_REQUIRED);
        DomainAssert.hasText(name, DivisionErrorCode.NAME_REQUIRED);
        return new Division(dojang, section, name, displayOrder);
    }

    public void updateName(String name) {
        DomainAssert.hasText(name, DivisionErrorCode.NAME_REQUIRED);
        this.name = name;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void updateSchedule(Set<DayOfWeek> scheduleDays, LocalTime startTime, LocalTime endTime) {
        this.scheduleDays = scheduleDays;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
