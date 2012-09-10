package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

/**
 */
public enum ContentPermissionType
{
    @RemoteName("View")
    VIEW,

    @RemoteName("Edit")
    EDIT
}
