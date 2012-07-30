package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.*;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppApplicationType;
import com.atlassian.labs.remoteapps.modules.permissions.Permissions;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.labs.remoteapps.util.ServletUtils;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTracker;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;

/**
 * Handles permissions for remote app operations
 */
@Component
public class PermissionManager
{
    private static final Logger log = LoggerFactory.getLogger(PermissionManager.class);
    private final UserManager userManager;
    private final SettingsManager settingsManager;
    private final PluginAccessor pluginAccessor;
    private final WaitableServiceTracker<String,ApiScope> apiScopeTracker;

    private final Set<String> NON_USER_ADMIN_PATHS = ImmutableSet.of(
        "/rest/remoteapps/latest/macro/",
        "/rest/remoteapps/1/macro/"
    );

    @Autowired
    public PermissionManager(
            UserManager userManager,
            WaitableServiceTrackerFactory waitableServiceTrackerFactory,
            SettingsManager settingsManager, PluginAccessor pluginAccessor)
    {
        this.userManager = userManager;
        this.settingsManager = settingsManager;
        this.pluginAccessor = pluginAccessor;
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

    public Iterable<ApiScope> getApiScopes()
    {
        return apiScopeTracker.getAll();
    }
    
    public void waitForApiScopes(Collection<String> scopeKeys)
    {
        try
        {
            apiScopeTracker.waitForKeys(scopeKeys).get(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            // ignore
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException("Unable to wait for scopes", e);
        }
        catch (TimeoutException e)
        {
            throw new PluginParseException("Unable to find all api scopes: " + scopeKeys);
        }
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

        final Set<String> appScopeKeys = getScopesForPlugin(clientKey);
        if (appScopeKeys != null)
        {
            Iterable<ApiScope> applicableScopes = Iterables.filter(apiScopeTracker.getAll(), new Predicate<ApiScope>()
            {
                @Override
                public boolean apply(ApiScope apiScope)
                {
                    return appScopeKeys.contains(apiScope.getKey());
                }
            });

            for (ApiScope scope : applicableScopes)
            {
                if (scope.allow(req, user))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<String> getScopesForPlugin(String clientKey)
    {
        Plugin plugin = pluginAccessor.getPlugin(clientKey);
        ModuleDescriptor<?> descriptor = plugin.getModuleDescriptor(
                "permissions");
        return descriptor != null ? ((Permissions)descriptor.getModule()).getPermissions() : Collections.<String>emptySet();
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
}
