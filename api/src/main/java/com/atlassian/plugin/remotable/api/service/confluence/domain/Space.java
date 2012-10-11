package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
