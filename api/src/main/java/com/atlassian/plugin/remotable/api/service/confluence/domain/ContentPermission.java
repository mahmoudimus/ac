package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
public interface ContentPermission
{
    ContentPermissionType getType();

    String getUserName();

    String getGroupName();
}
