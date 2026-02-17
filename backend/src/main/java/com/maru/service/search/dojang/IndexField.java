package com.maru.service.search.dojang;

import lombok.Getter;

@Getter
public enum IndexField {

    NAME(3.0),
    ADDRESS(1.5),
    OWNER(1.0);

    private final double boost;

    IndexField(double boost) {
        this.boost = boost;
    }

}
