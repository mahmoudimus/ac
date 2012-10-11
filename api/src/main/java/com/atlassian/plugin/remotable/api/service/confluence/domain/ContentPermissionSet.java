package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
public interface ContentPermissionSet
{
    ContentPermissionType getType();

    Iterable<ContentPermission> getContentPermissions();
}
