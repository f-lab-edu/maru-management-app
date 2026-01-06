package com.maru.repository.guardian.view;

import com.maru.domain.guardian.GuardianRelation;

public interface GuardianshipView {
    String getGuardianId();
    String getGuardianName();
    String getGuardianPhone();
    Boolean getIsVerified();
    GuardianRelation getRelation();
    Boolean getIsPrimary();
}
