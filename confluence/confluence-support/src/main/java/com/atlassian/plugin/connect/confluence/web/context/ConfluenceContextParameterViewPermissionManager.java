package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@ConfluenceComponent
public class ConfluenceContextParameterViewPermissionManager
{

    private PermissionManager permissionManager;
    private UserAccessor userAccessor;
    private UserManager userManager;

    @Autowired
    public ConfluenceContextParameterViewPermissionManager(PermissionManager permissionManager, UserAccessor userAccessor, UserManager userManager)
    {
        this.permissionManager = permissionManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
    }

    public boolean isParameterValueAccessibleByCurrentUser(Object contextValue)
    {
        ConfluenceUser currentUser = Optional.ofNullable(userManager.getRemoteUserKey())
                .map(userAccessor::getExistingUserByKey).orElse(null);
        return permissionManager.hasPermission(currentUser, Permission.VIEW, contextValue);
    }
}
