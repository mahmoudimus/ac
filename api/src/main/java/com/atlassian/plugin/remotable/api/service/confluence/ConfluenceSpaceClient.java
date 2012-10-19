package com.atlassian.plugin.remotable.api.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.domain.ExportType;
import com.atlassian.plugin.remotable.api.service.confluence.domain.GlobalPermission;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableSpace;
import com.atlassian.plugin.remotable.api.service.confluence.domain.Space;
import com.atlassian.plugin.remotable.api.service.confluence.domain.SpacePermission;
import com.atlassian.plugin.remotable.api.service.confluence.domain.SpaceSummary;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

import java.io.InputStream;

/**
 *
 */
public interface ConfluenceSpaceClient
{
    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<SpaceSummary>> getSpaces();

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Space> getSpace(String spaceKey);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<String>> getPermissions(String spaceKey);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<String>> getPermissions(String spaceKey, String userName);

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Space> addSpaceWithDefaultPermissions(MutableSpace space);

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Space> addSpace(MutableSpace space);

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Space> storeSpace(MutableSpace space);

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Space> addPersonalSpaceWithDefaultPermissions(MutableSpace space, String userName);

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Space> addPersonalSpace(Space space, String userName);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<SpacePermission>> getSpaceLevelPermissions();

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Boolean> addPermissionToSpace(SpacePermission spacePermission, String remoteEntityName, String spaceKey);

    // todo: add permission
    Promise<Void> removeGlobalPermission(GlobalPermission permission, String remoteEntityName);
    Promise<Boolean> addGlobalPermissions(Iterable<GlobalPermission> permissions, String remoteEntityName);

    @RequirePermission(ConfluencePermissions.MANAGE_ANONYMOUS_PERMISSIONS)
    Promise<Void> addAnonymousUsePermission();

    @RequirePermission(ConfluencePermissions.MANAGE_ANONYMOUS_PERMISSIONS)
    Promise<Void> removeAnonymousUsePermission();

    @RequirePermission(ConfluencePermissions.MANAGE_ANONYMOUS_PERMISSIONS)
    Promise<Boolean> addAnonymousViewUserProfilePermission();

    @RequirePermission(ConfluencePermissions.MANAGE_ANONYMOUS_PERMISSIONS)
    Promise<Void> removeAnonymousViewUserProfilePermission();

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Void> addPermissionsToSpace(Iterable<SpacePermission> permissions, String remoteEntityName, String spaceKey);

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Void> removePermissionFromSpace(SpacePermission permission, String remoteEntityName, String spaceKey);

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Void> removeSpace(String spaceKey);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<String> getSpaceStatus(String spaceKey);

    @RequirePermission(ConfluencePermissions.MODIFY_SPACES)
    Promise<Void> setSpaceStatus(String spaceKey, String statusValue);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<InputStream> exportSpace(String spaceKey, ExportType exportType);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<InputStream> exportSpace(String spaceKey, ExportType exportType, boolean exportAll);
}
