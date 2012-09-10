package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import java.util.List;

/**
 */
public interface ContentPermissionSet
{
    ContentPermissionType getType();

    Iterable<ContentPermission> getContentPermissions();
}
