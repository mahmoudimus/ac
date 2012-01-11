package com.atlassian.labs.remoteapps.modules.external;

/**
 * A remote module that executes code when the plugin has been explicitly uninstalled
 */
public interface UninstallableRemoteModule extends RemoteModule
{
    void uninstall();
}
