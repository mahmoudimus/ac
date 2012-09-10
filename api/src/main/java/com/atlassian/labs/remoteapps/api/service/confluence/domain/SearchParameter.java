package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

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
