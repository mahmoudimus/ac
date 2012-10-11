package com.atlassian.plugin.remotable.container;

import com.atlassian.plugin.remotable.host.common.HostProperties;

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
