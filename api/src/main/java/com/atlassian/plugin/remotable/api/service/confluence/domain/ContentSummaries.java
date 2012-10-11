package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

/**
 */
public interface ContentSummaries
{
    int getTotalAvailable();

    int getOffset();

    @RemoteName("content")
    Iterable<ContentSummary> getContentSummaries();
}
