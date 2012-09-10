package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

/**
 */
public enum ContentStatus
{
    @RemoteName("current")
    CURRENT,

    @RemoteName("deleted")
    DELETED,

    @RemoteName("modified")
    MODIFIED,

    @RemoteName("created")
    CREATED
}
