package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.*;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;

import java.io.InputStream;

/**
 *
 */
public interface ConfluenceSpaceClient
{
    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<SpaceSummary>> getSpaces();

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Space> getSpace(String spaceKey);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<String>> getPermissions(String spaceKey);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<String>> getPermissions(String spaceKey, String userName);

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Space> addSpaceWithDefaultPermissions(MutableSpace space);

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Space> addSpace(MutableSpace space);

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Space> storeSpace(MutableSpace space);

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Space> addPersonalSpaceWithDefaultPermissions(MutableSpace space, String userName);

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Space> addPersonalSpace(Space space, String userName);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<SpacePermission>> getSpaceLevelPermissions();

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Boolean> addPermissionToSpace(SpacePermission spacePermission, String remoteEntityName, String spaceKey);

    // todo: add permission
    Promise<Void> removeGlobalPermission(GlobalPermission permission, String remoteEntityName);
    Promise<Boolean> addGlobalPermissions(Iterable<GlobalPermission> permissions, String remoteEntityName);

    @RequirePermission(ConfluencePermission.MODIFY_ANONYMOUS_PERMISSIONS)
    Promise<Void> addAnonymousUsePermission();

    @RequirePermission(ConfluencePermission.MODIFY_ANONYMOUS_PERMISSIONS)
    Promise<Void> removeAnonymousUsePermission();

    @RequirePermission(ConfluencePermission.MODIFY_ANONYMOUS_PERMISSIONS)
    Promise<Boolean> addAnonymousViewUserProfilePermission();

    @RequirePermission(ConfluencePermission.MODIFY_ANONYMOUS_PERMISSIONS)
    Promise<Void> removeAnonymousViewUserProfilePermission();

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Void> addPermissionsToSpace(Iterable<SpacePermission> permissions, String remoteEntityName, String spaceKey);

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Void> removePermissionFromSpace(SpacePermission permission, String remoteEntityName, String spaceKey);

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Void> removeSpace(String spaceKey);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<String> getSpaceStatus(String spaceKey);

    @RequirePermission(ConfluencePermission.MODIFY_SPACES)
    Promise<Void> setSpaceStatus(String spaceKey, String statusValue);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<InputStream> exportSpace(String spaceKey, ExportType exportType);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<InputStream> exportSpace(String spaceKey, ExportType exportType, boolean exportAll);
}
