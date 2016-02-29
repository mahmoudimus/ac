package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ModuleType(ListableModuleDescriptorFactory.class)
@Component
public final class ConnectModuleProviderModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectModuleProviderModuleDescriptor> {

    private static final String TYPE = "connect-module";

    @Autowired
    public ConnectModuleProviderModuleDescriptorFactory(HostContainer hostContainer) {
        super(hostContainer, TYPE, ConnectModuleProviderModuleDescriptor.class);
    }
}
