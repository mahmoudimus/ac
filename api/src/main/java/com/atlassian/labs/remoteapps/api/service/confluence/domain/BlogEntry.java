package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.net.URI;

/**
 */
public interface BlogEntry
{
    long getId();

    @RemoteName("space")
    String getSpaceKey();

    String getTitle();

    URI getUrl();

    int getVersion();

    String getContent();

    int getLocks();
}
