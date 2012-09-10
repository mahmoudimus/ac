package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
public interface ContentPermission
{
    ContentPermissionType getType();

    String getUserName();

    String getGroupName();
}
