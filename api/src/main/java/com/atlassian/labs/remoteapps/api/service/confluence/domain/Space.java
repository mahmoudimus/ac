package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.net.URI;

/**
 */
public interface Space
{
    String getName();

    String getKey();

    URI getUrl();

    String getDescription();

    @RemoteName("homePage")
    Long getHomePageId();
}
