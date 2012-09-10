package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.util.Date;

/**
 */
public interface ContentSummary
{
    long getId();

    ContentType getType();

    @RemoteName("space")
    String getSpaceKey();

    ContentStatus getStatus();

    String getTitle();

    Date getCreated();

    @RemoteName("creator")
    String getCreatorName();

    Date getModified();

    @RemoteName("modifier")
    String getModifierName();
}
