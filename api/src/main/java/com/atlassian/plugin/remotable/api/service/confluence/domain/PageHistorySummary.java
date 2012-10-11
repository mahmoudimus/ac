package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
