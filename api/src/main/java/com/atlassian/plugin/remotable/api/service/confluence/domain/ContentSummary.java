package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
