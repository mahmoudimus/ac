package com.atlassian.plugin.connect.spi.permission;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * a good default implementation for permissions.
 *
 * @since 0.8
 */
@XmlDescriptor
public abstract class AbstractPermission implements Permission
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

    @Override
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
