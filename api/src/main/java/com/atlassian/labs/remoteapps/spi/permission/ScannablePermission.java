package com.atlassian.labs.remoteapps.spi.permission;

import com.atlassian.labs.remoteapps.spi.InstallationFailedException;

/**
 *
 */
public interface ScannablePermission extends Permission
{
    boolean needsPermission(PluginScanner scanner) throws InstallationFailedException;
}
