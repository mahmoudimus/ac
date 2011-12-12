package com.atlassian.labs.remoteapps.modules.external;

/**
 * A remote module that wants to execute code on remote app descriptor shutdown
 */
public interface ClosableRemoteModule extends RemoteModule
{
    void close();
}
