package com.maru.controller.groups.dto;

import java.util.List;

public record GroupListRes(
        List<GroupRes> groups
) {
    public static GroupListRes from(List<GroupRes> groups) {
        return new GroupListRes(groups);
    }
}
