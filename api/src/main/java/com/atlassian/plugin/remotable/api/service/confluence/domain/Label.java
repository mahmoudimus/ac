package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

/**
 */
public interface Label
{
    String getName();

    @RemoteName("owner")
    String getOwnerName();

    String getNamespace();

    long getId();
}
