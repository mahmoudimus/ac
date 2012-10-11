package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
