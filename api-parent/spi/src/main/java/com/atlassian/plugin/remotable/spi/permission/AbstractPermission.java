package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

import static com.atlassian.plugin.remotable.api.InstallationMode.LOCAL;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * a good default implementation for permissions.
 *
 * @since 0.8
 */
public abstract class AbstractPermission implements Permission
{
    public static final Set<InstallationMode> DEFAULT_INSTALLATION_MODES = ImmutableSet.of(LOCAL);

    private final String key;
    private final ImmutableSet<InstallationMode> installationModes;
    private final PermissionInfo permissionInfo;

    protected AbstractPermission(String key)
    {
        this(key, ImmutableSet.of(InstallationMode.LOCAL));
    }

    protected AbstractPermission(String key, Set<InstallationMode> installationModes)
    {
        this(key, installationModes, EmptyPermissionInfo.INSTANCE);
    }

    protected AbstractPermission(String key, Set<InstallationMode> installationModes, PermissionInfo permissionInfo)
    {
        this.key = checkNotNull(key);
        this.installationModes = ImmutableSet.copyOf(checkNotNull(installationModes));
        this.permissionInfo = checkNotNull(permissionInfo);
    }

    @Override
    public final String getKey()
    {
        return key;
    }

    @Override
    public final Set<InstallationMode> getInstallationModes()
    {
        return installationModes;
    }

    @Override
    public final String getName()
    {
        return permissionInfo.getName();
    }

    @Override
    public final String getDescription()
    {
        return permissionInfo.getDescription();
    }

    @Override
    public final PermissionInfo getPermissionInfo()
    {
        return permissionInfo;
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractPermission))
        {
            return false;
        }

        final AbstractPermission that = (AbstractPermission) o;

        return key.equals(that.key);
    }

    @Override
    public final int hashCode()
    {
        return key.hashCode();
    }

    private static enum EmptyPermissionInfo implements PermissionInfo
    {
        INSTANCE;

        @Override
        public String getName()
        {
            return "";
        }

        @Override
        public String getDescription()
        {
            return "";
        }


    }
}
