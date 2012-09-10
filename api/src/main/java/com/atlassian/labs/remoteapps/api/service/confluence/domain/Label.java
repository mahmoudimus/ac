package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

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
