package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.remotable.spi.InstallationFailedException;

public interface ScannablePermission extends Permission
{
    boolean needsPermission(PluginScanner scanner) throws InstallationFailedException;
}
