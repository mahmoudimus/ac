package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.net.URI;
import java.util.Date;

/**
 */
public interface Comment
{
    long getId();

    long getPageId();

    String getTitle();

    String getContent();

    URI getUrl();

    Date getCreated();

    @RemoteName("creator")
    String getCreatorName();
}
