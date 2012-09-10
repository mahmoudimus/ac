package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.util.Date;

/**
 */
public interface PageHistorySummary
{
    long getId();

    int getVersion();

    @RemoteName("modifier")
    String getModifierName();

    Date getModified();

    String getVersionComment();
}
