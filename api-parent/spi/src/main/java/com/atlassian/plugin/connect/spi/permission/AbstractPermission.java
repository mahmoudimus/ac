package com.atlassian.plugin.connect.spi.permission;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractPermission
{
    private final String key;
    private final PermissionInfo permissionInfo;

    protected AbstractPermission(String key)
    {
        this(key, EmptyPermissionInfo.INSTANCE);
    }

    protected AbstractPermission(String key, PermissionInfo permissionInfo)
    {
        this.key = checkNotNull(key);
        this.permissionInfo = checkNotNull(permissionInfo);
    }

    public final String getKey()
    {
        return key;
    }

    public final String getName()
    {
        return permissionInfo.getName();
    }

    public final String getDescription()
    {
        return permissionInfo.getDescription();
    }

    public boolean equals(Object o)
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

    public int hashCode()
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
