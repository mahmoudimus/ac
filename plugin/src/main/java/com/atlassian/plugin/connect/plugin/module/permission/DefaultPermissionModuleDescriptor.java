package com.atlassian.plugin.connect.plugin.module.permission;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.permission.DefaultPermission;
import com.atlassian.plugin.connect.spi.permission.Permission;
import com.atlassian.plugin.connect.spi.permission.PermissionInfo;
import com.atlassian.plugin.connect.spi.permission.PermissionModuleDescriptor;
import com.atlassian.plugin.connect.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.permission.scope.ApiScope;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DefaultPermissionModuleDescriptor extends AbstractModuleDescriptor<Permission> implements PermissionModuleDescriptor
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Supplier<? extends Permission> permissionSupplier;

    public DefaultPermissionModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        checkHasDefinePluginPermissionPermission(plugin);

        super.init(plugin, element);
        final PermissionInfo permissionInfo = getPermissionInfo();

        permissionSupplier = Suppliers.memoize(new Supplier<Permission>()
        {
            @Override
            public Permission get()
            {
                final Permission permission;
                if (moduleClassName == null)
                {
                    permission = new DefaultPermission(getKey(), permissionInfo);
                }
                else
                {
                    final Permission permissionFromModuleClassName = getPermissionFromModuleClassName();
                    if (permissionFromModuleClassName instanceof ApiScope)
                    {
                        permission = new ForwardingApiScope((ApiScope) permissionFromModuleClassName, permissionInfo);
                    }
                    else
                    {
                        permission = new ForwardingPermission<Permission>(permissionFromModuleClassName, permissionInfo);
                    }
                }
                return permission;
            }
        });
    }

    private Permission getPermissionFromModuleClassName()
    {
        return moduleFactory.createModule(moduleClassName, DefaultPermissionModuleDescriptor.this);
    }

    private void checkHasDefinePluginPermissionPermission(Plugin plugin)
    {
        boolean hasAllPermissions;
        try
        {
            hasAllPermissions = plugin.hasAllPermissions();
        }
        catch (NoSuchMethodError error)
        {
            // we're on Atlassian Plugins v2, where permissions are not supported, so plugins have all permissions
            logger.debug("We're using Atlassian Plugins 2, plugins have all permissions, will allow {}", Permissions.DEFINE_PLUGIN_PERMISSION, error);
            hasAllPermissions = true;
        }

        if (hasAllPermissions)
        {
            return;
        }

        if (!plugin.getActivePermissions().contains(Permissions.DEFINE_PLUGIN_PERMISSION))
        {
            throw new PermissionDeniedException(
                    plugin.getKey(),
                    String.format("Could not define permission, as plugin doesn't have the required '%s' permission", Permissions.DEFINE_PLUGIN_PERMISSION));
        }
    }

    // todo: support i18n
    private PermissionInfo getPermissionInfo()
    {
        return new DefaultPermissionInfo(getName(), getDescription());
    }

    @Override
    public Permission getModule()
    {
        return permissionSupplier.get();
    }

    private static final class DefaultPermissionInfo implements PermissionInfo
    {
        private final String name;
        private final String description;

        private DefaultPermissionInfo(String name, String description)
        {
            this.name = name;
            this.description = description;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getDescription()
        {
            return description;
        }
    }

    private static class ForwardingPermission<P extends Permission> implements Permission
    {
        protected final P delegate;
        private final PermissionInfo permissionInfo;

        private ForwardingPermission(P delegate, PermissionInfo permissionInfo)
        {
            this.delegate = checkNotNull(delegate);
            this.permissionInfo = checkNotNull(permissionInfo);
        }

        @Override
        public String getKey()
        {
            return delegate.getKey();
        }

        @Override
        public PermissionInfo getPermissionInfo()
        {
            return permissionInfo;
        }

        @Override
        public String getName()
        {
            return permissionInfo.getName();
        }

        @Override
        public String getDescription()
        {
            return permissionInfo.getDescription();
        }
    }

    private static final class ForwardingApiScope extends ForwardingPermission<ApiScope> implements ApiScope
    {
        private ForwardingApiScope(ApiScope delegate, PermissionInfo permissionInfo)
        {
            super(delegate, permissionInfo);
        }

        @Override
        public boolean allow(HttpServletRequest request, UserKey user)
        {
            return delegate.allow(request, user);
        }

        @Override
        public Iterable<ApiResourceInfo> getApiResourceInfos()
        {
            return delegate.getApiResourceInfos();
        }
    }
    
    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
    
}
