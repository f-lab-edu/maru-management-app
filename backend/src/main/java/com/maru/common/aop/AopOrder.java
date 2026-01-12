package com.maru.common.aop;

import org.springframework.core.Ordered;

public final class AopOrder {

    private AopOrder() {}

    public static final int SECURITY_VALIDATION = Ordered.HIGHEST_PRECEDENCE + 100;
    public static final int PERMISSION_CHECK = Ordered.HIGHEST_PRECEDENCE + 200;
}
