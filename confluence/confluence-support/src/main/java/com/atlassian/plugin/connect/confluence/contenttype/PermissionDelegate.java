package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.user.User;

public class PermissionDelegate implements com.atlassian.confluence.security.PermissionDelegate
{
    @Override
    public boolean canView(User user, Object o)
    {
        return true;
    }

    @Override
    public boolean canView(User user)
    {
        return true;
    }

    @Override
    public boolean canEdit(User user, Object o)
    {
        return true;
    }

    @Override
    public boolean canSetPermissions(User user, Object o)
    {
        return true;
    }

    @Override
    public boolean canRemove(User user, Object o)
    {
        return true;
    }

    @Override
    public boolean canExport(User user, Object o)
    {
        return true;
    }

    @Override
    public boolean canAdminister(User user, Object o)
    {
        return true;
    }

    @Override
    public boolean canCreate(User user, Object o)
    {
        return true;
    }

    @Override
    public boolean canCreateInTarget(User user, Class aClass)
    {
        return true;
    }
}
