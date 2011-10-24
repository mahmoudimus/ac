package com.atlassian.labs.remoteapps.installer;

/**
 *
 */
public interface RemoteAppInstaller
{
    void install(String registrationUrl, String registrationSecret);
}
