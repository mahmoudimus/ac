package com.atlassian.plugin.connect.plugin.usermanagement.confluence;

import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.Set;

import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ScopeChange;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.COMMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CONFLUENCE_ADMINISTRATOR_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CREATEEDIT_PAGE_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CREATE_ATTACHMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.EDITBLOG_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_ATTACHMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_BLOG_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_COMMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_PAGE_PERMISSION;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.ADMIN;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.DELETE;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.READ;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.SPACE_ADMIN;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.WRITE;
import static com.google.common.collect.ImmutableSet.Builder;

public class ConfluencePermissionChange
{
    private final Set<String> addedSpacePermissions;
    private final Set<String> addedGlobalPermissions;
    private final Set<String> removedSpacePermissions;
    private final Set<String> removedGlobalPermissions;

    private final Map<ScopeName, Set<String>> spacePermissionMap = ImmutableMap.<ScopeName, Set<String>>of(
            READ, set(SpacePermission.VIEWSPACE_PERMISSION),

            WRITE, set(CREATEEDIT_PAGE_PERMISSION, CREATE_ATTACHMENT_PERMISSION, COMMENT_PERMISSION,
                EDITBLOG_PERMISSION),

            DELETE, set(REMOVE_PAGE_PERMISSION, REMOVE_ATTACHMENT_PERMISSION, REMOVE_COMMENT_PERMISSION,
                REMOVE_BLOG_PERMISSION),

            SPACE_ADMIN, set(ADMINISTER_SPACE_PERMISSION)
    );

    private static Set<String> set(String... permissions)
    {
        return ImmutableSet.of(permissions);
    }

    public ConfluencePermissionChange(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        final ScopeChange scopeChange = new ScopeChange(previousScopes, newScopes);
        addedSpacePermissions = mapToSpacePermissions(scopeChange.getAddedScopes());
        addedGlobalPermissions = mapToGlobalPermissions(scopeChange.getAddedScopes());
        removedSpacePermissions = mapToSpacePermissions(scopeChange.getRemovedScopes());
        removedGlobalPermissions = mapToGlobalPermissions(scopeChange.getRemovedScopes());
    }

    private Set<String> mapToSpacePermissions(Set<ScopeName> scopes)
    {
        final Builder<String> builder = ImmutableSet.builder();
        for (ScopeName scope : scopes)
        {
            builder.addAll(mapToSpacePermissions(scope));
        }
        return builder.build();
    }

    private Set<String> mapToSpacePermissions(ScopeName scope)
    {
        return spacePermissionMap.get(scope);
    }


    private Set<String> mapToGlobalPermissions(Set<ScopeName> scopes)
    {
        return scopes.contains(ADMIN) ? set(CONFLUENCE_ADMINISTRATOR_PERMISSION) : set();
    }

    public Set<String> getAddedSpacePermissions()
    {
        return addedSpacePermissions;
    }

    public Set<String> getAddedGlobalPermissions()
    {
        return addedGlobalPermissions;
    }

    public Set<String> getRemovedSpacePermissions()
    {
        return removedSpacePermissions;
    }

    public Set<String> getRemovedGlobalPermissions()
    {
        return removedGlobalPermissions;
    }
}
