package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;

import java.util.Map;

/**
 *
 */
public class CanInstallRemoteAppsCondition implements Condition
{
    private final PermissionManager permissionManager;
    private final UserManager userManager;

    public CanInstallRemoteAppsCondition(PermissionManager permissionManager, UserManager userManager)
    {
        this.permissionManager = permissionManager;
        this.userManager = userManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return permissionManager.canInstallRemoteApps(userManager.getRemoteUsername());
    }
}
