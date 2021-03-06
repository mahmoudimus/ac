package com.atlassian.plugin.connect.plugin.web.blacklist;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ModuleType(ListableModuleDescriptorFactory.class)
@Component
public class ConnectWebFragmentLocationBlacklistModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectWebFragmentLocationBlacklistModuleDescriptor> {
    @Autowired
    public ConnectWebFragmentLocationBlacklistModuleDescriptorFactory(HostContainer hostContainer) {
        super(hostContainer, "connect-web-fragment-location-blacklist", ConnectWebFragmentLocationBlacklistModuleDescriptor.class);
    }
}
