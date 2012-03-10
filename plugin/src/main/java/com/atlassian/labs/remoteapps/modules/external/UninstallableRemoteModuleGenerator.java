package com.atlassian.labs.remoteapps.modules.external;

/**
 * A remote module generator that executes code after any plugin has been explicitly uninstalled
 */
public interface UninstallableRemoteModuleGenerator extends RemoteModuleGenerator
{
    void uninstall(String pluginKey);
}
