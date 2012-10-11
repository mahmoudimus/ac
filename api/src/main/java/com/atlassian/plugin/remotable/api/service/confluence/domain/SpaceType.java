package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

/**
 */
public enum SpaceType
{
    @RemoteName("global")
    GLOBAL,

    @RemoteName("personal")
    PERSONAL
}
