package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

/**
 */
public enum ContentPermissionType
{
    @RemoteName("View")
    VIEW,

    @RemoteName("Edit")
    EDIT
}
