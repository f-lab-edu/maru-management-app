package com.maru.controller.section.dto;

import java.util.List;

public record SectionListRes(
        List<SectionRes> sections
) {
    public static SectionListRes from(List<SectionRes> sections) {
        return new SectionListRes(sections);
    }
}
