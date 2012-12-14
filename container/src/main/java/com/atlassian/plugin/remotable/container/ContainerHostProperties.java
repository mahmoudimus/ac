package com.atlassian.plugin.remotable.container;

import com.atlassian.plugin.remotable.spi.host.HostProperties;

public final class ContainerHostProperties implements HostProperties
{
    @Override
    public String getKey()
    {
        return "container";
    }
}
