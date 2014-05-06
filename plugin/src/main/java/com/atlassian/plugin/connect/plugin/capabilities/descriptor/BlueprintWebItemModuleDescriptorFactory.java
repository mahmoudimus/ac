package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.springframework.stereotype.Component;

/**
 * The {@link com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean} to
 * {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor} part of the blueprint
 * mapping.
 *
 * @see com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintModuleDescriptorFactory
 */
@Component
public class BlueprintWebItemModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<BlueprintModuleBean, WebItemModuleDescriptor>
{
    @Override
    public WebItemModuleDescriptor createModuleDescriptor(ConnectAddonBean addon, Plugin plugin, BlueprintModuleBean bean) {
        return null;
    }
}
