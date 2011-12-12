package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevel;
import com.atlassian.labs.remoteapps.descriptor.external.ApiScopeModuleDescriptor;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Handles permissions for remote app operations
 */
@Component
public class PermissionManager implements DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(PermissionManager.class);
    public static final String API_SCOPES_LINK_KEY = "api-scopes";
    private final ApplicationLinkService applicationLinkService;
    private final OAuthLinkManager linkManager;
    private final UserManager userManager;
    private final AccessLevelManager accessLevelManager;
    private final PluginModuleTracker<ApiScope, ApiScopeModuleDescriptor> apiScopeTracker;
    private static final String ACCESS_LEVEL_KEY = "access-level";

    @Autowired
    public PermissionManager(ApplicationLinkService applicationLinkService,
                             OAuthLinkManager linkManager,
                             UserManager userManager,
                             PluginEventManager pluginEventManager,
                             PluginAccessor pluginAccessor,
                             AccessLevelManager accessLevelManager
    )
    {
        this.applicationLinkService = applicationLinkService;
        this.linkManager = linkManager;
        this.userManager = userManager;
        this.accessLevelManager = accessLevelManager;
        this.apiScopeTracker = new DefaultPluginModuleTracker<ApiScope, ApiScopeModuleDescriptor>(pluginAccessor, pluginEventManager,
                ApiScopeModuleDescriptor.class);
    }

    public void setApiPermissions(ApplicationType type, List<String> scopes)
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(type.getClass());
        if (scopes.isEmpty())
        {
            link.removeProperty(API_SCOPES_LINK_KEY);
        }
        else
        {
            link.putProperty(API_SCOPES_LINK_KEY, scopes);
        }
    }

    public void setRestrictRemoteApp(ApplicationType type, AccessLevel accessLevel)
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(type.getClass());
        if (link == null)
        {
            throw new IllegalStateException("Application link cannot be found for type '" + type.getI18nKey() + "'");
        }
        link.putProperty(ACCESS_LEVEL_KEY, accessLevel.getId());
    }

    public boolean canCurrentUserAccessRemoteApp(HttpServletRequest request, ApplicationLink link)
    {
        return canAccessRemoteApp(userManager.getRemoteUsername(request), link);
    }

    public boolean canAccessRemoteApp(String username, ApplicationLink link)
    {
        AccessLevel accessLevel = accessLevelManager.getAccessLevel(
                (String)link.getProperty(ACCESS_LEVEL_KEY));

        return accessLevel.canAccessRemoteApp(username, link);
    }

    public boolean canAccessApi(String userId, String consumerKey)
    {
        ApplicationLink link = linkManager.getLinkForOAuthClientKey(consumerKey);
        return canAccessRemoteApp(userId, link);

    }

    public Iterable<ApiScopeModuleDescriptor> getApiScopeDescriptors()
    {
        return apiScopeTracker.getModuleDescriptors();
    }

    @Override
    public void destroy() throws Exception
    {
        apiScopeTracker.close();
    }

    public boolean isRequestInApiScope(HttpServletRequest req, String clientKey, String user)
    {
        ApplicationLink link = linkManager.getLinkForOAuthClientKey(clientKey);
        List<String> apiScopes = (List<String>) link.getProperty(API_SCOPES_LINK_KEY);
        if (apiScopes != null)
        {
            for (ApiScope scope : apiScopeTracker.getModules())
            {
                if (scope.allow(req, user))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canInstallRemoteApps(String username)
    {
        // todo: make configurable
        return username != null &&
                (userManager.isUserInGroup(username, "developers") ||
                 userManager.isAdmin(username));
    }
}
