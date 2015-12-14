package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ModuleType (ListableModuleDescriptorFactory.class)
@Component
public class MovableWebPanelLocationProviderModuleDescriptorFactory extends SingleModuleDescriptorFactory<MovableWebPanelLocationProviderModuleDescriptor>
{
    @Autowired
    public MovableWebPanelLocationProviderModuleDescriptorFactory(HostContainer hostContainer)
    {
        super(hostContainer, "movable-web-panel-locations-list", MovableWebPanelLocationProviderModuleDescriptor.class);
    }
}
