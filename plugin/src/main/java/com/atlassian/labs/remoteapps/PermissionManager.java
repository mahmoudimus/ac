package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.*;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppApplicationType;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.labs.remoteapps.util.ServletUtils;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTracker;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTrackerFactory;
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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles permissions for remote app operations
 */
@Component
public class PermissionManager
{
    private static final Logger log = LoggerFactory.getLogger(PermissionManager.class);
    public static final String API_SCOPES_LINK_KEY = "api-scopes";
    private final ApplicationLinkAccessor applicationLinkAccessor;
    private final OAuthLinkManager linkManager;
    private final UserManager userManager;
    private final SettingsManager settingsManager;
    private final WaitableServiceTracker<String,ApiScope> apiScopeTracker;

    private final Set<String> NON_USER_ADMIN_PATHS = ImmutableSet.of(
        "/rest/remoteapps/latest/macro/",
        "/rest/remoteapps/1/macro/"
    );

    @Autowired
    public PermissionManager(ApplicationLinkAccessor applicationLinkAccessor,
            OAuthLinkManager linkManager,
            UserManager userManager,
            WaitableServiceTrackerFactory waitableServiceTrackerFactory,
            SettingsManager settingsManager)
    {
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.linkManager = linkManager;
        this.userManager = userManager;
        this.settingsManager = settingsManager;
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

    public void setApiPermissions(ApplicationType type, List<String> scopes)
    {
        ApplicationLink link = applicationLinkAccessor.getApplicationLink(type);
        link.putProperty(API_SCOPES_LINK_KEY, scopes);
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

        ApplicationLink link = getLinkForClientKey(clientKey);
        if (link == null)
        {
            return false;
        }

        final List<String> appScopeKeys = (List<String>) link.getProperty(API_SCOPES_LINK_KEY);
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

    public ApplicationLink getLinkForClientKey(String clientKey)
    {
        ApplicationLink link = linkManager.getLinkForOAuthClientKey(clientKey);
        if (link == null)
        {
            // fallback to checking all app links
            for (ApplicationLink aLink : applicationLinkAccessor.getApplicationLinks())
            {
                ApplicationType type = aLink.getType();
                if (type instanceof RemoteAppApplicationType)
                {
                    RemoteAppApplicationType raType = (RemoteAppApplicationType) type;
                    if (clientKey.equals(raType.getId().get()))
                    {
                        link = aLink;
                    }
                }
            }
        }
        return link;
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
