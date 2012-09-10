package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.util.List;

/**
 */
public interface ContentSummaries
{
    int getTotalAvailable();

    int getOffset();

    @RemoteName("content")
    Iterable<ContentSummary> getContentSummaries();
}
