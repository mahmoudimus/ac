package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ModuleType(ListableModuleDescriptorFactory.class)
@Component
public class ConnectConditionClassResolverModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectConditionClassResolverModuleDescriptor> {

    private static final String TYPE = "connect-condition-class-resolver";

    @Autowired
    public ConnectConditionClassResolverModuleDescriptorFactory(HostContainer hostContainer) {
        super(hostContainer, TYPE, ConnectConditionClassResolverModuleDescriptor.class);
    }
}
