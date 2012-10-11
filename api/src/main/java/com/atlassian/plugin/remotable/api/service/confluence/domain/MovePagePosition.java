package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
