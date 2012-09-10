package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.net.URI;
import java.util.Date;

/**
 */
public interface BlogEntrySummary
{
    long getId();

    @RemoteName("space")
    String getSpaceKey();

    String getTitle();

    URI getUrl();

    int getLocks();

    Date getPublishDate();
}
