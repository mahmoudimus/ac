package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
