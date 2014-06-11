package com.atlassian.plugin.connect.spi.permission;

import com.atlassian.plugin.connect.spi.xmldescriptor.XmlDescriptor;

@XmlDescriptor
public final class DefaultPermission extends AbstractPermission
{
    public DefaultPermission(String key, PermissionInfo permissionInfo)
    {
        super(key, permissionInfo);
    }
}
