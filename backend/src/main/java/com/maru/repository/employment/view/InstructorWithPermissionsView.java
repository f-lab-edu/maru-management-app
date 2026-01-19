package com.maru.repository.employment.view;

import com.maru.domain.permission.PermissionType;

import java.util.Set;

public interface InstructorWithPermissionsView extends InstructorView {
    String getPermissions();
}
