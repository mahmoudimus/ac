package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.remotable.api.InstallationMode;

import java.util.Set;

public final class DefaultPermission extends AbstractPermission
{
    public DefaultPermission(String key, Set<InstallationMode> installationModes, PermissionInfo permissionInfo)
    {
        super(key, installationModes, permissionInfo);
    }
}
