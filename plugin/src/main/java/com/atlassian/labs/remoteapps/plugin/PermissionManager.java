package com.atlassian.labs.remoteapps.plugin;

import com.atlassian.labs.remoteapps.plugin.module.permission.PermissionsReader;
import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import com.atlassian.labs.remoteapps.spi.permission.Permission;
import com.atlassian.labs.remoteapps.spi.permission.PermissionModuleDescriptor;
import com.atlassian.labs.remoteapps.spi.permission.scope.ApiScope;
import com.atlassian.labs.remoteapps.plugin.settings.SettingsManager;
import com.atlassian.labs.remoteapps.spi.util.ServletUtils;
import com.atlassian.labs.remoteapps.plugin.util.tracker.WaitableServiceTracker;
import com.atlassian.labs.remoteapps.plugin.util.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Set;

/**
 * Handles permissions for remote app operations
 */
@Component
public class PermissionManager
{
    private final UserManager userManager;
    private final SettingsManager settingsManager;
    private final PluginAccessor pluginAccessor;
    private final PermissionsReader permissionsReader;
    private final WaitableServiceTracker<String,ApiScope> apiScopeTracker;
    private final DefaultPluginModuleTracker<Permission, PermissionModuleDescriptor> permissionTracker;

    private final Set<String> NON_USER_ADMIN_PATHS = ImmutableSet.of(
        "/rest/remoteapps/latest/macro/",
        "/rest/remoteapps/1/macro/"
    );

    @Autowired
    public PermissionManager(
            UserManager userManager,
            WaitableServiceTrackerFactory waitableServiceTrackerFactory,
            SettingsManager settingsManager, PluginAccessor pluginAccessor,
            PluginEventManager pluginEventManager,
            PermissionsReader permissionsReader)
    {
        this.userManager = userManager;
        this.settingsManager = settingsManager;
        this.pluginAccessor = pluginAccessor;
        this.permissionsReader = permissionsReader;
        this.permissionTracker = new DefaultPluginModuleTracker<Permission, PermissionModuleDescriptor>(
                pluginAccessor, pluginEventManager, PermissionModuleDescriptor.class);
        this.apiScopeTracker = waitableServiceTrackerFactory.create(ApiScope.class,
                new Function<ApiScope, String>()
                {
                    @Override
                    public String apply(ApiScope from)
                    {
                        return from.getKey();
                    }
                });
    }

    public Iterable<Permission> getPermissions()
    {
        return Iterables.concat(permissionTracker.getModules(), apiScopeTracker.getAll());
    }
    
    public boolean isRequestInApiScope(HttpServletRequest req, String clientKey, String user)
    {
        // check for non-user admin request
        if (user == null)
        {
            String pathInfo = ServletUtils.extractPathInfo(req);
            for (String adminPath : NON_USER_ADMIN_PATHS)
            {
                if (pathInfo.startsWith(adminPath))
                {
                    return true;
                }
            }
        }

        final Set<String> permissions = getPermissionsForPlugin(clientKey);
        Iterable<ApiScope> applicableScopes = Iterables.filter(apiScopeTracker.getAll(), new Predicate<ApiScope>()
        {
            @Override
            public boolean apply(ApiScope apiScope)
            {
                return permissions.contains(apiScope.getKey());
            }
        });

        for (ApiScope scope : applicableScopes)
        {
            if (scope.allow(req, user))
            {
                return true;
            }
        }
        return false;
    }

    private Set<String> getPermissionsForPlugin(String clientKey)
    {
        Plugin plugin = pluginAccessor.getPlugin(clientKey);
        return plugin != null ? permissionsReader.getPermissionsForPlugin(plugin)
                : Collections.<String>emptySet();
    }

    public boolean canInstallRemoteApps(String username)
    {
        return username != null &&

                // for OnDemand dogfooding
                ((settingsManager.isAllowDogfooding() && inDogfoodingGroup(username)) ||

                 // the default
                 userManager.isSystemAdmin(username));
    }

    private boolean inDogfoodingGroup(String username)
    {
        // for OnDemand dogfooding
        return userManager.isUserInGroup(username, "developers") ||

                // for internal Atlassian dogfooding
                userManager.isUserInGroup(username, "atlassian-staff") ||

                // for smoke tests
                userManager.isUserInGroup(username, "test-users");
    }

    public void requirePermission(String pluginKey, String permissionKey) throws PermissionDeniedException
    {
        if (!getPermissionsForPlugin(pluginKey).contains(permissionKey))
        {
            throw new PermissionDeniedException("Required permission '" + permissionKey + "' must be requested " +
                "for this plugin '" + pluginKey + "'");
        }
    }
}
