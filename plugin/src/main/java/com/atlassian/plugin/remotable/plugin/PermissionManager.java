package com.atlassian.plugin.remotable.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.plugin.remotable.plugin.product.ProductAccessor;
import com.atlassian.plugin.remotable.plugin.settings.SettingsManager;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.permission.Permission;
import com.atlassian.plugin.remotable.spi.permission.PermissionModuleDescriptor;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.util.ServletUtils;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Document;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Set;

import static com.atlassian.plugin.remotable.host.common.util.RemotablePluginManifestReader
        .getInstallerUser;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Handles permissions for remote plugin operations
 */
@Component
public class PermissionManager
{
    private final UserManager userManager;
    private final SettingsManager settingsManager;
    private final PluginAccessor pluginAccessor;
    private final ProductAccessor productAccessor;
    private final PermissionsReader permissionsReader;
    private final BundleContext bundleContext;
    private final DefaultPluginModuleTracker<Permission, PermissionModuleDescriptor> permissionTracker;
    private static final Logger log = LoggerFactory.getLogger(PermissionManager.class);

    private final Set<String> NON_USER_ADMIN_PATHS = ImmutableSet.of(
        "/rest/remotable-plugins/latest/macro/",
        "/rest/remotable-plugins/1/macro/"
    );

    @Autowired
    public PermissionManager(
            UserManager userManager,
            SettingsManager settingsManager, PluginAccessor pluginAccessor,
            PluginEventManager pluginEventManager,
            ProductAccessor productAccessor, PermissionsReader permissionsReader,
            BundleContext bundleContext)
    {
        this.userManager = userManager;
        this.settingsManager = settingsManager;
        this.pluginAccessor = pluginAccessor;
        this.productAccessor = productAccessor;
        this.permissionsReader = permissionsReader;
        this.bundleContext = bundleContext;
        this.permissionTracker = new DefaultPluginModuleTracker<Permission, PermissionModuleDescriptor>(
                pluginAccessor, pluginEventManager, PermissionModuleDescriptor.class);
    }

    public Iterable<Permission> getPermissions()
    {
        return permissionTracker.getModules();
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
        Iterable<ApiScope> applicableScopes = transform(filter(permissionTracker.getModules(), new Predicate<Permission>()
        {
            @Override
            public boolean apply(@Nullable Permission input)
            {
                return input instanceof ApiScope && permissions.contains(input.getKey());
            }
        }), new Function<Permission, ApiScope>()
        {
            @Override
            public ApiScope apply(@Nullable Permission input)
            {
                return (ApiScope)input;
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

    public boolean canInstallRemotePluginsFromMarketplace(String username)
    {
        return username != null &&

                // for OnDemand dogfooding
                (isDogfoodUser(username) ||

                 // the default
                 userManager.isAdmin(username));
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
            throw new PermissionDeniedException(pluginKey, "Required permission '" + permissionKey + "' must be requested " +
                "for this plugin '" + pluginKey + "'");
        }
    }

    public boolean hasPermission(String pluginKey, String permissionKey) throws PermissionDeniedException
    {
        return getPermissionsForPlugin(pluginKey).contains(permissionKey);
    }

    public boolean canModifyRemotePlugin(String username, String pluginKey)
    {
        if (userManager.isAdmin(username))
        {
            return true;
        }

        if (isDogfoodUser(username) && username.equals(
                getInstallerUser(BundleUtil.findBundleForPlugin(bundleContext, pluginKey))))
        {
            return true;
        }

        return false;
    }

    private boolean isDogfoodUser(String username)
    {
        return settingsManager.isAllowDogfooding() && inDogfoodingGroup(username);
    }

    public boolean canRequestDeclaredPermissions(String username, Document descriptor, InstallationMode installationMode)
    {
        if (userManager.isSystemAdmin(username))
        {
            return true;
        }

        Set<String> requestedPermissions = permissionsReader.readPermissionsFromDescriptor(descriptor, installationMode);

        return productAccessor.getAllowedPermissions(installationMode).containsAll(requestedPermissions);
    }

    public boolean canInstallArbitraryRemotePlugins(String userName)
    {
        return userManager.isSystemAdmin(userName) || isDogfoodUser(userName);
    }
}
