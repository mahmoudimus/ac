package com.atlassian.plugin.connect.stash.iframe.context;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.iframe.context.AbstractModuleContextFilter;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionService;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import org.springframework.beans.factory.annotation.Autowired;

@StashComponent
public class StashModuleContextFilter extends AbstractModuleContextFilter<StashUser>
{
    public static final String PROJECT_ID = "project.id";
    public static final String PROJECT_KEY = "projectKey";
    public static final String REPOSITORY_ID = "repository.id";

    private final StashAuthenticationContext authenticationContext;
    private final PermissionService permissionService;
    private final Iterable<PermissionCheck<StashUser>> permissionChecks;

    @Autowired
    public StashModuleContextFilter(
            final StashAuthenticationContext authenticationContext,
            final PermissionService permissionService,
            final PluginAccessor pluginAccessor)
    {
        super(pluginAccessor, StashUser.class);

        this.authenticationContext = authenticationContext;
        this.permissionService = permissionService;

        permissionChecks = constructPermissionChecks();
    }

    @Override
    protected StashUser getCurrentUser()
    {
        return authenticationContext.getCurrentUser();
    }

    @Override
    protected Iterable<PermissionCheck<StashUser>> getPermissionChecks()
    {
        return permissionChecks;
    }

    private Iterable<PermissionCheck<StashUser>> constructPermissionChecks()
    {
        return ImmutableList.of(
                new PermissionChecks.LongValue<StashUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return REPOSITORY_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final StashUser user)
                    {

                        return permissionService.hasRepositoryPermission(user, Ints.saturatedCast(id), Permission.REPO_READ);
                    }
                },
                new PermissionChecks.LongValue<StashUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return PROJECT_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final StashUser user)
                    {

                        return permissionService.hasProjectPermission(user, Ints.saturatedCast(id), Permission.PROJECT_VIEW);
                    }
                },
                // users must be logged in to see another user's profile
                PermissionChecks.<StashUser>mustBeLoggedIn(PROFILE_NAME),
                PermissionChecks.<StashUser>mustBeLoggedIn(PROFILE_KEY)
        );
    }
}
