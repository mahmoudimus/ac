package com.atlassian.plugin.connect.bitbucket.iframe.context;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.iframe.context.AbstractModuleContextFilter;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.user.ApplicationUser;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import org.springframework.beans.factory.annotation.Autowired;

@BitbucketComponent
public class BitbucketModuleContextFilter extends AbstractModuleContextFilter<ApplicationUser>
{
    public static final String PROJECT_ID = "project.id";
    public static final String PROJECT_KEY = "projectKey";
    public static final String REPOSITORY_ID = "repository.id";

    private final AuthenticationContext authenticationContext;
    private final PermissionService permissionService;
    private final Iterable<PermissionCheck<ApplicationUser>> permissionChecks;

    @Autowired
    public BitbucketModuleContextFilter(
            final AuthenticationContext authenticationContext,
            final PermissionService permissionService,
            final PluginAccessor pluginAccessor)
    {
        super(pluginAccessor, ApplicationUser.class);

        this.authenticationContext = authenticationContext;
        this.permissionService = permissionService;

        permissionChecks = constructPermissionChecks();
    }

    @Override
    protected ApplicationUser getCurrentUser()
    {
        return authenticationContext.getCurrentUser();
    }

    @Override
    protected Iterable<PermissionCheck<ApplicationUser>> getPermissionChecks()
    {
        return permissionChecks;
    }

    private Iterable<PermissionCheck<ApplicationUser>> constructPermissionChecks()
    {
        return ImmutableList.of(
                new PermissionChecks.LongValue<ApplicationUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return REPOSITORY_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user)
                    {

                        return permissionService.hasRepositoryPermission(user, Ints.saturatedCast(id), Permission.REPO_READ);
                    }
                },
                new PermissionChecks.LongValue<ApplicationUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return PROJECT_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user)
                    {

                        return permissionService.hasProjectPermission(user, Ints.saturatedCast(id), Permission.PROJECT_VIEW);
                    }
                },
                // users must be logged in to see another user's profile
                PermissionChecks.<ApplicationUser>mustBeLoggedIn(PROFILE_NAME),
                PermissionChecks.<ApplicationUser>mustBeLoggedIn(PROFILE_KEY)
        );
    }
}
