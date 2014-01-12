package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.service.IsDevModeService;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.permission.Permission;
import com.atlassian.plugin.connect.spi.permission.PermissionModuleDescriptor;
import com.atlassian.plugin.connect.spi.permission.PermissionsReader;
import com.atlassian.plugin.connect.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.ApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;

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
    private final PluginAccessor pluginAccessor;
    private final PermissionsReader permissionsReader;
    private final PluginModuleTracker<Permission, PermissionModuleDescriptor> permissionTracker;

    private final Set<ApiScope> DEFAULT_API_SCOPES = ImmutableSet.<ApiScope>of(new MacroCacheApiScope());
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final IsDevModeService isDevModeService;

    @Autowired
    public PermissionManagerImpl(
            PluginAccessor pluginAccessor,
            PluginEventManager pluginEventManager,
            PermissionsReader permissionsReader,
            IsDevModeService isDevModeService,
            JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService)
    {
        this(pluginAccessor, permissionsReader, isDevModeService, jsonConnectAddOnIdentifierService,
                new DefaultPluginModuleTracker<Permission, PermissionModuleDescriptor>(
                        pluginAccessor, pluginEventManager, PermissionModuleDescriptor.class));
    }

    PermissionManagerImpl(
            PluginAccessor pluginAccessor,
            PermissionsReader permissionsReader,
            IsDevModeService isDevModeService,
            JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService,
            PluginModuleTracker<Permission, PermissionModuleDescriptor> pluginModuleTracker)
    {
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
        this.isDevModeService = isDevModeService;
        this.pluginAccessor = checkNotNull(pluginAccessor);
        this.permissionsReader = checkNotNull(permissionsReader);
        this.permissionTracker = checkNotNull(pluginModuleTracker);
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
    public boolean isRequestInApiScope(HttpServletRequest req, String pluginKey, String username)
    {
        return any(getApiScopesForPlugin(pluginKey), new IsInApiScopePredicate(req, username));
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

    private Set<String> getPermissionsForPlugin(String pluginKey)
    {
        Set<String> permissions = Sets.newHashSet();
        if (jsonConnectAddOnIdentifierService.isConnectAddOn(pluginKey) && isDevModeService.isDevMode())
        {
            // Connect Add-Ons provided by JSON descriptors are allowed all scopes (ACDEV-679)
            permissions.addAll(getPermissionKeys());
        }
        else
        {
            Plugin plugin = pluginAccessor.getPlugin(pluginKey);
            if (plugin != null)
            {
                permissions.addAll(permissionsReader.getPermissionsForPlugin(plugin));
            }
        }
        return permissions;
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

    private static final class IsInApiScopePredicate implements Predicate<ApiScope>
    {
        private final HttpServletRequest request;
        private final String username;

        public IsInApiScopePredicate(HttpServletRequest request, @Nullable String username)
        {
            this.request = checkNotNull(request);
            this.username = username;
        }

        @Override
        public boolean apply(ApiScope scope)
        {
            return scope.allow(request, username);
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
