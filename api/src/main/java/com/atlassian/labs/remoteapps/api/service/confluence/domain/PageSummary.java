package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.net.URI;

/**
 */
public interface PageSummary
{
    long getId();

    @RemoteName("space")
    String getSpaceKey();

    Long getParentId();

    String getTitle();

    URI getUrl();

    int getLocks();
}
