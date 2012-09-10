package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.net.URI;
import java.util.Date;

/**
 */
public interface Page
{
    long getId();

    @RemoteName("space")
    String getSpaceKey();

    Long getParentId();

    String getTitle();

    URI getUrl();

    int getVersion();

    String getContent();

    Date getCreated();

    @RemoteName("creator")
    String getCreatorName();

    Date getModified();

    @RemoteName("modifier")
    String getModifierName();

    boolean isHomePage();

    int getLocks();

    ContentStatus getContentStatus();

    boolean isCurrent();
}
