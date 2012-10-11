package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
