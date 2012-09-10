package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

/**
 */
public enum MovePagePosition
{
    @RemoteName("above")
    ABOVE,

    @RemoteName("below")
    BELOW,

    @RemoteName("append")
    APPEND,

    @RemoteName("topLevel")
    TOP_LEVEL
}
