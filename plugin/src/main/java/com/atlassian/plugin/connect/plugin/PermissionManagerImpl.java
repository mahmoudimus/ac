package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Suppliers;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.scopes.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;
import com.atlassian.plugin.connect.plugin.scopes.StaticAddOnScopes;
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
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    private final PluginAccessor pluginAccessor;
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final PermissionsReader permissionsReader;
    private final PluginModuleTracker<Permission, PermissionModuleDescriptor> permissionTracker;
    private final Collection<AddOnScope> allScopes;
    private final IsDevModeService isDevModeService;

    @Deprecated
    private static final Set<ApiScope> DEFAULT_OLD_API_SCOPES = ImmutableSet.<ApiScope>of(new MacroCacheApiScope());

    private static final Logger log = LoggerFactory.getLogger(PermissionManagerImpl.class);

    @Autowired
    public PermissionManagerImpl(
            PluginAccessor pluginAccessor,
            JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService,
            PluginEventManager pluginEventManager,
            PermissionsReader permissionsReader,
            IsDevModeService isDevModeService,
            ApplicationProperties applicationProperties) throws IOException
    {
        this(pluginAccessor, jsonConnectAddOnIdentifierService, permissionsReader, isDevModeService, applicationProperties,
             new DefaultPluginModuleTracker<Permission, PermissionModuleDescriptor>(pluginAccessor, pluginEventManager, PermissionModuleDescriptor.class));
    }

    PermissionManagerImpl(
            PluginAccessor pluginAccessor,
            JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService,
            PermissionsReader permissionsReader,
            IsDevModeService isDevModeService,
            ApplicationProperties applicationProperties,
            PluginModuleTracker<Permission, PermissionModuleDescriptor> pluginModuleTracker) throws IOException
    {
        this.pluginAccessor = checkNotNull(pluginAccessor);
        this.jsonConnectAddOnIdentifierService = checkNotNull(jsonConnectAddOnIdentifierService);
        this.permissionsReader = checkNotNull(permissionsReader);
        this.isDevModeService = checkNotNull(isDevModeService);
        this.permissionTracker = checkNotNull(pluginModuleTracker);
        this.allScopes = buildScopes(applicationProperties.getDisplayName());
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
        boolean isInScopes = any(getApiScopesForPlugin(pluginKey), new IsInApiScopePredicate(req, user));

        // Connect Add-Ons provided by JSON descriptors are allowed all scopes (ACDEV-679)
        if (!isInScopes && isDevModeService.isDevMode() && jsonConnectAddOnIdentifierService.isConnectAddOn(pluginKey))
        {
            log.warn(String.format("Dev mode: allowing plugin '%s' to access '%s' for user '%s'", pluginKey, req.getRequestURI(), user.getStringValue()));
            isInScopes = true;
        }

        return isInScopes;
    }

    private static Collection<AddOnScope> buildScopes(String applicationDisplayName) throws IOException
    {
        if (StringUtils.isEmpty(applicationDisplayName))
        {
            throw new IllegalArgumentException("Application display name can be neither null nor blank");
        }

        String lowerCaseDisplayName = applicationDisplayName.toLowerCase();

        if (lowerCaseDisplayName.contains("confluence"))
        {
            return StaticAddOnScopes.buildForConfluence();
        }

        if (lowerCaseDisplayName.contains("jira"))
        {
            return StaticAddOnScopes.buildForJira();
        }

        // alternately we could send the display name straight through to StaticAddOnScopes.buildFor(String)
        // but with a name like "display name" I'm not confident that it won't contain formatting or extra characters
        throw new IllegalArgumentException(String.format("Application display name '%s' is not recognised as either Confluence or JIRA. Please set it to a value that when converted to lower case contains either 'confluence' or 'jira'.", applicationDisplayName));
    }

    private Iterable<? extends ApiScope> getApiScopesForPlugin(String pluginKey)
    {
        return jsonConnectAddOnIdentifierService.isConnectAddOn(pluginKey)
                ? StaticAddOnScopes.dereference(allScopes, addImpliedScopesTo(getScopeReferences(pluginKey)))
                : Iterables.concat(DEFAULT_OLD_API_SCOPES, getApiScopesForPermissions(getOldStylePermissionsForPlugin(pluginKey)));
    }

    @Deprecated
    private Iterable<ApiScope> getApiScopesForPermissions(final Set<String> permissions)
    {
        return castToApiScopes(getApiScopesForPermissionsAsPermissions(permissions));
    }

    @Deprecated
    private static Iterable<ApiScope> castToApiScopes(Iterable<Permission> permissions)
    {
        return transform(permissions, new CastPermissionApiScope());
    }

    @Deprecated
    private Iterable<Permission> getApiScopesForPermissionsAsPermissions(Set<String> permissions)
    {
        return filter(permissionTracker.getModules(), Predicates.and(new IsApiScope(), new IsInPermissions(permissions)));
    }

    private static Collection<ScopeName> addImpliedScopesTo(Set<ScopeName> scopeReferences)
    {
        Set<ScopeName> allScopeReferences = new HashSet<ScopeName>(scopeReferences);

        for (ScopeName scopeReference : scopeReferences)
        {
            allScopeReferences.addAll(scopeReference.getImplied());
        }

        return allScopeReferences;
    }

    private Set<ScopeName> getScopeReferences(String pluginKey)
    {
        return option(pluginAccessor.getPlugin(pluginKey)).fold(
                Suppliers.ofInstance(ImmutableSet.<ScopeName>of()),
                new Function<Plugin, Set<ScopeName>>()
                {
                    @Override
                    public Set<ScopeName> apply(Plugin plugin)
                    {
                        return permissionsReader.readScopesForAddOn(plugin);
                    }
                });
    }

    @Deprecated
    private Set<String> getOldStylePermissionsForPlugin(String pluginKey)
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

    /**
     * @deprecated Used only in OAuth and email sending, both of which are themselves deprecated.
     */
    @Deprecated
    @Override
    public void requirePermission(String pluginKey, String permissionKey) throws PermissionDeniedException
    {
        if (!getOldStylePermissionsForPlugin(pluginKey).contains(permissionKey))
        {
            throw new PermissionDeniedException(pluginKey,
                    format("Plugin '%s' requires a resource protected by '%s', but it did not request it.", pluginKey, permissionKey));
        }
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

    @Deprecated
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

    @Deprecated
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
