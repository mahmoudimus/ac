package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.user.User;

public class PermissionDelegate implements com.atlassian.confluence.security.PermissionDelegate {
    private final PermissionManager permissionManager;

    public PermissionDelegate(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean canView(User user, Object o) {
        return true;
    }

    @Override
    public boolean canView(User user) {
        return true;
    }

    @Override
    public boolean canEdit(User user, Object o) {
        return true;
    }

    @Override
    public boolean canSetPermissions(User user, Object o) {
        return true;
    }

    @Override
    public boolean canRemove(User user, Object o) {
        return true;
    }

    @Override
    public boolean canExport(User user, Object o) {
        return true;
    }

    @Override
    public boolean canAdminister(User user, Object o) {
        return true;
    }

    @Override
    public boolean canCreate(User user, Object o) {
        return true;
    }

    @Override
    public boolean canCreateInTarget(User user, Class aClass) {
        return true;
    }
}
