package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.descriptor.external.ApiScopeModuleDescriptor;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.util.ServletUtils;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

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
    private final PluginModuleTracker<ApiScope, ApiScopeModuleDescriptor> apiScopeTracker;

    private final Set<String> NON_USER_ADMIN_PATHS = ImmutableSet.of(
        "/rest/remoteapps/latest/macro/",
        "/rest/remoteapps/1/macro/"
    );

    @Autowired
    public PermissionManager(ApplicationLinkService applicationLinkService,
                             OAuthLinkManager linkManager,
                             UserManager userManager,
                             PluginEventManager pluginEventManager,
                             PluginAccessor pluginAccessor)
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
            link.removeProperty(API_SCOPES_LINK_KEY);
        }
        else
        {
            link.putProperty(API_SCOPES_LINK_KEY, scopes);
        }
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

        ApplicationLink link = linkManager.getLinkForOAuthClientKey(clientKey);
        if (link == null)
        {
            return false;
        }

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

                // for OnDemand dogfooding
                (userManager.isUserInGroup(username, "developers") ||

                 // for internal Atlassian dogfooding
                 userManager.isUserInGroup(username, "atlassian-staff") ||

                 // for smoke tests
                 userManager.isUserInGroup(username, "test-users") ||

                 // the default
                 userManager.isAdmin(username));
    }
}
