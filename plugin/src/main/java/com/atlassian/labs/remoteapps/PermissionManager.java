package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScopeModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public class PermissionManager implements DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(PermissionManager.class);
    private final ApplicationLinkService applicationLinkService;
    private final OAuthLinkManager linkManager;
    private final UserManager userManager;
    private final PluginModuleTracker<ApiScope, ApiScopeModuleDescriptor> apiScopeTracker;

    public PermissionManager(ApplicationLinkService applicationLinkService,
                             OAuthLinkManager linkManager,
                             UserManager userManager,
                             PluginEventManager pluginEventManager, PluginAccessor pluginAccessor)
    {
        this.applicationLinkService = applicationLinkService;
        this.linkManager = linkManager;
        this.userManager = userManager;
        this.apiScopeTracker = new DefaultPluginModuleTracker<ApiScope, ApiScopeModuleDescriptor>(pluginAccessor, pluginEventManager,
                ApiScopeModuleDescriptor.class);
    }

    public void setApiPermissions(ApplicationType type, List<String> scopes)
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(type.getClass());
        if (scopes.isEmpty())
        {
            link.removeProperty("api-scopes");
        }
        else
        {
            link.putProperty("api-scopes", scopes);
        }
    }

    public boolean canCurrentUserAccessRemoteApp(HttpServletRequest request, String consumerKey)
    {
        return canAccessRemoteApp(userManager.getRemoteUsername(request), consumerKey);
    }

    public boolean canAccessRemoteApp(String username, String consumerKey)
    {
        return !userManager.isSystemAdmin(username);
    }

    public boolean canAccessApi(String userId, String consumerKey)
    {
        return canAccessRemoteApp(userId, consumerKey);

    }

    @Override
    public void destroy() throws Exception
    {
        apiScopeTracker.close();
    }

    public boolean isRequestInApiScope(HttpServletRequest req, String clientKey)
    {
        ApplicationLink link = linkManager.getLinkForOAuthClientKey(clientKey);
        List<String> apiScopes = (List<String>) link.getProperty("api-scopes");
        if (apiScopes != null)
        {
            for (ApiScope scope : apiScopeTracker.getModules())
            {
                if (scope.allow(req))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
