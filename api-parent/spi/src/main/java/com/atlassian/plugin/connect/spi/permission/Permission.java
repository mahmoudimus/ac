package com.atlassian.plugin.connect.spi.permission;

import com.atlassian.plugin.connect.spi.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.schema.spi.SchemaDocumented;

@XmlDescriptor
public interface Permission extends SchemaDocumented
{
    String getKey();

    /**
     * @return some permission info.
     * @since 0.8
     */
    PermissionInfo getPermissionInfo();
}
