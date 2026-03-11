package com.maru.support;

import com.maru.security.TenantContextHolder;

public final class TenantTestSupport {

    public static final String TEST_TENANT_ID = "TEST_TENANT";
    public static final String TEST_DOJANG_ID = "TEST_DOJANG";
    public static final String TEST_USER_ID = "TEST_USER";
    public static final String TEST_ROLE = "OWNER";

    private TenantTestSupport() {
    }

    public static void setContext() {
        setContext(TEST_TENANT_ID, TEST_DOJANG_ID, TEST_USER_ID, TEST_ROLE);
    }

    public static void setContext(String tenantId, String dojangId, String userId, String role) {
        TenantContextHolder.withContext(tenantId, userId, dojangId, role);
    }

    public static void clearContext() {
        TenantContextHolder.clear();
    }
}
