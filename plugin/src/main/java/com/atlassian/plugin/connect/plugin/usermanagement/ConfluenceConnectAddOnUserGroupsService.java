package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupsService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.user.UserManager;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unused")
@ConfluenceComponent
public class ConfluenceConnectAddOnUserGroupsService implements ConnectAddOnUserGroupsService
{
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("confluence-users");

    //private final PermissionManager confluencePermissionManager;
    //private final UserManager userManager;

    /*
    @Inject
    public ConfluenceConnectAddOnUserGroupsService(PermissionManager confluencePermissionManager, UserManager userManager)
    {
        this.confluencePermissionManager = confluencePermissionManager;
        this.userManager = userManager;
    }
    */

    @Override
    public Set<String> getDefaultProductGroups()
    {
        // As reported by Sam Day, without the "confluence-users" group the add-on user can't
        // even get the page summary of a page that is open to anonymous access.
        return GROUPS;
    }

    @Override
    public void ensureIsAdmin(String groupKey)
    {
        if (!isAdmin(groupKey))
        {
            //confluencePermissionManager.
        }
    }

    @Override
    public boolean isAdmin(String groupKey)
    {
        checkNotNull(groupKey);
        return false;//confluencePermissionManager.
    }
}
