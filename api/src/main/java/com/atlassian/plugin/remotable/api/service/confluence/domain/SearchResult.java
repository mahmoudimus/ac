package com.atlassian.plugin.remotable.api.service.confluence.domain;

import java.net.URI;

/**
 */
public interface SearchResult
{
    String getTitle();

    URI getUrl();

    String getExcerpt();

    ContentType getType();

    Long getId();
}
