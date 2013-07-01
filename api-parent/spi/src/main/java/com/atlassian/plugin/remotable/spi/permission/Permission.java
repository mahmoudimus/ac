package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.schema.spi.SchemaDocumented;

import java.util.Set;

public interface Permission extends SchemaDocumented
{
    String getKey();

    /**
     * @return some permission info.
     * @since 0.8
     */
    PermissionInfo getPermissionInfo();

    /**
     * <p>The installation modes for which this permission is allowed.
     *
     * @return a set of allowed installation modes.
     * @since 0.8
     */
    Set<InstallationMode> getInstallationModes();
}
