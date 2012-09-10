package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

/**
 */
public enum SpaceType
{
    @RemoteName("global")
    GLOBAL,

    @RemoteName("personal")
    PERSONAL
}
