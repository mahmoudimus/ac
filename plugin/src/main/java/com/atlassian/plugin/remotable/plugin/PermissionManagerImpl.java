package com.atlassian.plugin.remotable.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.plugin.remotable.plugin.settings.SettingsManager;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.permission.Permission;
import com.atlassian.plugin.remotable.spi.permission.PermissionModuleDescriptor;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.util.ServletUtils;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Document;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Set;

import static com.atlassian.plugin.remotable.host.common.util.RemotablePluginManifestReader.getInstallerUser;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Handles permissions for remote plugin operations
 */
@Component
public final class PermissionManagerImpl implements PermissionManager
{
    private final UserManager userManager;
    private final SettingsManager settingsManager;
    private final PluginAccessor pluginAccessor;
    private final PermissionsReader permissionsReader;
    private final BundleContext bundleContext;
    private final PluginModuleTracker<Permission, PermissionModuleDescriptor> permissionTracker;

    private final Set<String> NON_USER_ADMIN_PATHS = ImmutableSet.of(
            "/rest/remotable-plugins/latest/macro/",
            "/rest/remotable-plugins/1/macro/"
    );

    @Autowired
    public PermissionManagerImpl(
            UserManager userManager,
            SettingsManager settingsManager,
            PluginAccessor pluginAccessor,
            PluginEventManager pluginEventManager,
            PermissionsReader permissionsReader,
            BundleContext bundleContext)
    {
        this(userManager, settingsManager, pluginAccessor, permissionsReader, bundleContext,
                new DefaultPluginModuleTracker<Permission, PermissionModuleDescriptor>(
                        pluginAccessor, pluginEventManager, PermissionModuleDescriptor.class));
    }

    PermissionManagerImpl(
            UserManager userManager,
            SettingsManager settingsManager,
            PluginAccessor pluginAccessor,
            PermissionsReader permissionsReader,
            BundleContext bundleContext,
            PluginModuleTracker<Permission, PermissionModuleDescriptor> pluginModuleTracker)
    {
        this.userManager = checkNotNull(userManager);
        this.settingsManager = checkNotNull(settingsManager);
        this.pluginAccessor = checkNotNull(pluginAccessor);
        this.permissionsReader = checkNotNull(permissionsReader);
        this.bundleContext = checkNotNull(bundleContext);
        this.permissionTracker = checkNotNull(pluginModuleTracker);
    }

    @Override
    public Set<Permission> getPermissions(final InstallationMode mode)
    {
        return copyOf(filter(permissionTracker.getModules(), new Predicate<Permission>()
        {
            @Override
            public boolean apply(Permission p)
            {
                return p.getInstallationModes().contains(mode);
            }
        }));
    }

    @Override
    public Set<String> getPermissionKeys(InstallationMode mode)
    {
        return copyOf(transform(getPermissions(mode), new Function<Permission, String>()
        {
            @Override
            public String apply(Permission p)
            {
                return p.getKey();
            }
        }));
    }

    @Override
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
                return (ApiScope) input;
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

    @Override
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

    @Override
    public void requirePermission(String pluginKey, String permissionKey) throws PermissionDeniedException
    {
        if (!getPermissionsForPlugin(pluginKey).contains(permissionKey))
        {
            throw new PermissionDeniedException(pluginKey, "Required permission '" + permissionKey + "' must be requested " +
                    "for this plugin '" + pluginKey + "'");
        }
    }

    @Override
    public boolean hasPermission(String pluginKey, String permissionKey) throws PermissionDeniedException
    {
        return getPermissionsForPlugin(pluginKey).contains(permissionKey);
    }

    @Override
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

    @Override
    public boolean canRequestDeclaredPermissions(String username, Document descriptor, InstallationMode installationMode)
    {
        if (userManager.isSystemAdmin(username))
        {
            return true;
        }

        Set<String> requestedPermissions = permissionsReader.readPermissionsFromDescriptor(descriptor, installationMode);

        return getPermissionKeys(installationMode).containsAll(requestedPermissions);
    }

    @Override
    public boolean canInstallArbitraryRemotePlugins(String userName)
    {
        return userManager.isSystemAdmin(userName) || isDogfoodUser(userName);
    }
}
