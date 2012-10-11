package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

/**
 */
public enum SearchParameter
{
    @RemoteName("spaceKey")
    SPACE_KEY,

    @RemoteName("type")
    TYPE,

    @RemoteName("modified")
    MODIFIED,

    @RemoteName("contributor")
    CONTRIBUTOR
}
