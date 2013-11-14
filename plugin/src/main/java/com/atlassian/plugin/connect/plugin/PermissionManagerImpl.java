package com.atlassian.plugin.connect.plugin;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.plugin.settings.SettingsManager;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.permission.Permission;
import com.atlassian.plugin.connect.spi.permission.PermissionModuleDescriptor;
import com.atlassian.plugin.connect.spi.permission.PermissionsReader;
import com.atlassian.plugin.connect.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.ApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.dom4j.Document;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.*;
import static java.lang.String.format;

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
    private final ConnectAddOnIdentifierService connectIdentifier;

    private final Set<ApiScope> DEFAULT_API_SCOPES = ImmutableSet.<ApiScope>of(new MacroCacheApiScope());

    @Autowired
    public PermissionManagerImpl(
            UserManager userManager,
            SettingsManager settingsManager,
            PluginAccessor pluginAccessor,
            PluginEventManager pluginEventManager,
            PermissionsReader permissionsReader,
            BundleContext bundleContext,
            ConnectAddOnIdentifierService connectIdentifier)
    {
        this(userManager, settingsManager, pluginAccessor, permissionsReader, bundleContext,connectIdentifier,
                new DefaultPluginModuleTracker<Permission, PermissionModuleDescriptor>(
                        pluginAccessor, pluginEventManager, PermissionModuleDescriptor.class));
    }

    PermissionManagerImpl(
            UserManager userManager,
            SettingsManager settingsManager,
            PluginAccessor pluginAccessor,
            PermissionsReader permissionsReader,
            BundleContext bundleContext,
            ConnectAddOnIdentifierService connectIdentifier,
            PluginModuleTracker<Permission, PermissionModuleDescriptor> pluginModuleTracker)
    {
        this.userManager = checkNotNull(userManager);
        this.settingsManager = checkNotNull(settingsManager);
        this.pluginAccessor = checkNotNull(pluginAccessor);
        this.permissionsReader = checkNotNull(permissionsReader);
        this.bundleContext = checkNotNull(bundleContext);
        this.permissionTracker = checkNotNull(pluginModuleTracker);
        this.connectIdentifier = checkNotNull(connectIdentifier);
    }

    @Override
    public Set<Permission> getPermissions()
    {
        return copyOf(permissionTracker.getModules());
    }

    @Override
    public Set<String> getPermissionKeys()
    {
        return copyOf(transform(getPermissions(), new Function<Permission, String>()
        {
            @Override
            public String apply(Permission p)
            {
                return p.getKey();
            }
        }));
    }

    @Override
    public boolean isRequestInApiScope(HttpServletRequest req, String pluginKey, UserKey user)
    {
        return any(getApiScopesForPlugin(pluginKey), new IsInApiScopePredicate(req, user));
    }

    private Iterable<ApiScope> getApiScopesForPlugin(String pluginKey)
    {
        return Iterables.concat(DEFAULT_API_SCOPES, getApiScopesForPermissions(getPermissionsForPlugin(pluginKey)));
    }

    private Iterable<ApiScope> getApiScopesForPermissions(final Set<String> permissions)
    {
        return castToApiScopes(getApiScopesForPermissionsAsPermissions(permissions));
    }

    private Iterable<ApiScope> castToApiScopes(Iterable<Permission> permissions)
    {
        return transform(permissions, new CastPermissionApiScope());
    }

    private Iterable<Permission> getApiScopesForPermissionsAsPermissions(Set<String> permissions)
    {
        return filter(permissionTracker.getModules(), Predicates.and(new IsApiScope(), new IsInPermissions(permissions)));
    }

    private Set<String> getPermissionsForPlugin(String clientKey)
    {
        return option(pluginAccessor.getPlugin(clientKey)).fold(
                Suppliers.ofInstance(ImmutableSet.<String>of()),
                new Function<Plugin, Set<String>>()
                {
                    @Override
                    public Set<String> apply(Plugin plugin)
                    {
                        return permissionsReader.getPermissionsForPlugin(plugin);
                    }
                });
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
            throw new PermissionDeniedException(pluginKey,
                    format("Plugin '%s' requires a resource protected by '%s', but it did not request it.", pluginKey, permissionKey));
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
        return userManager.isAdmin(username)
                || isDogfoodUser(username)
                && username.equals(connectIdentifier.getInstallerUser(BundleUtil.findBundleForPlugin(bundleContext, pluginKey)));
    }

    private boolean isDogfoodUser(String username)
    {
        return settingsManager.isAllowDogfooding() && inDogfoodingGroup(username);
    }

    @Override
    public boolean canRequestDeclaredPermissions(String username, Document descriptor)
    {
        if (userManager.isSystemAdmin(username))
        {
            return true;
        }

        Set<String> requestedPermissions = permissionsReader.readPermissionsFromDescriptor(descriptor);

        return getPermissionKeys().containsAll(requestedPermissions);
    }

    @Override
    public boolean canInstallArbitraryRemotePlugins(String userName)
    {
        return userManager.isSystemAdmin(userName) || isDogfoodUser(userName);
    }

    private static final class IsInApiScopePredicate implements Predicate<ApiScope>
    {
        private final HttpServletRequest request;
        private final UserKey user;

        public IsInApiScopePredicate(HttpServletRequest request, @Nullable UserKey user)
        {
            this.request = checkNotNull(request);
            this.user = user;
        }

        @Override
        public boolean apply(ApiScope scope)
        {
            return scope.allow(request, user);
        }
    }

    private static final class IsApiScope implements Predicate<Permission>
    {
        @Override
        public boolean apply(@Nullable Permission permission)
        {
            return permission instanceof ApiScope;
        }
    }

    private static final class IsInPermissions implements Predicate<Permission>
    {
        private final Set<String> permissions;

        public IsInPermissions(Set<String> permissions)
        {
            this.permissions = checkNotNull(permissions);
        }

        @Override
        public boolean apply(@Nullable Permission permission)
        {
            return permission != null && permissions.contains(permission.getKey());
        }
    }

    private static final class CastPermissionApiScope implements Function<Permission, ApiScope>
    {
        @Override
        public ApiScope apply(Permission permission)
        {
            return (ApiScope) permission;
        }
    }

    private static final class MacroCacheApiScope extends AbstractApiScope
    {
        public MacroCacheApiScope()
        {
            super("clear_macro_cache", new RestApiScopeHelper(Arrays.asList(
                    new RestApiScopeHelper.RestScope("atlassian-connect", Arrays.asList("latest", "1"), "/macro", Arrays.asList("DELETE"))
            )));
        }
    }
}
