package com.atlassian.labs.remoteapps.spi.permission;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

/**
 *
 */
public class PermissionModuleDescriptor extends AbstractModuleDescriptor<Permission>
{
    private Permission permission;

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element
    ) throws PluginParseException
    {
        super.init(plugin, element);
        if (moduleClassName == null)
        {
            // todo: support i18n better
            permission = new DefaultPermission(getKey(), getName(), getDescription());
        }
    }

    @Override
    public Permission getModule()
    {
        return permission != null ? permission : moduleFactory.createModule(moduleClassName, this);
    }
}
