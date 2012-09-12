package com.atlassian.labs.remoteapps.container;

import com.atlassian.labs.remoteapps.host.common.HostProperties;

/**
 *
 */
public class ContainerHostProperties implements HostProperties
{
    @Override
    public String getKey()
    {
        return "container";
    }
}
