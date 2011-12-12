package com.atlassian.labs.remoteapps.modules.external;

/**
 * A remote module that executes code on when the plugin and system have started
 */
public interface StartableRemoteModule extends RemoteModule
{
    void start();
}
