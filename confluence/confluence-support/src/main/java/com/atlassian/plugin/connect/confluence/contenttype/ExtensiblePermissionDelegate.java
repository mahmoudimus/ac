package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.security.PermissionDelegate;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.user.User;

@ConfluenceComponent
public class ExtensiblePermissionDelegate implements PermissionDelegate
{
    @Override
    public boolean canView(User user, Object o)
    {
        return false;
    }

    @Override
    public boolean canView(User user)
    {
        return false;
    }

    @Override
    public boolean canEdit(User user, Object o)
    {
        return false;
    }

    @Override
    public boolean canSetPermissions(User user, Object o)
    {
        return false;
    }

    @Override
    public boolean canRemove(User user, Object o)
    {
        return false;
    }

    @Override
    public boolean canExport(User user, Object o)
    {
        return false;
    }

    @Override
    public boolean canAdminister(User user, Object o)
    {
        return false;
    }

    @Override
    public boolean canCreate(User user, Object o)
    {
        return false;
    }

    @Override
    public boolean canCreateInTarget(User user, Class aClass)
    {
        return false;
    }
}
