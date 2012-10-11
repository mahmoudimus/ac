package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.remotable.spi.permission.scope.MutablePermission;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.dom4j.Element;

/**
 *
 */
public class PermissionModuleDescriptor extends AbstractModuleDescriptor<Permission>
{
    private Supplier<? extends Permission> permissionSupplier;

    public PermissionModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element
    ) throws PluginParseException
    {
        super.init(plugin, element);
        permissionSupplier = Suppliers.memoize(new Supplier<Permission>()
        {
            @Override
            public Permission get()
            {
                Permission permission = null;
                if (moduleClassName == null)
                {
                    permission = new DefaultPermission(getKey());
                }
                else
                {
                    permission = moduleFactory.createModule(moduleClassName, PermissionModuleDescriptor.this);
                }
                if (permission instanceof MutablePermission)
                {
                    // todo: support i18n
                    ((MutablePermission) permission).setName(getName());
                    ((MutablePermission) permission).setDescription(getDescription());
                }
                return permission;
            }
        });

    }

    @Override
    public Permission getModule()
    {
        return permissionSupplier.get();
    }
}
