package com.atlassian.plugin.connect.plugin.condition;

import com.atlassian.plugin.connect.spi.condition.ConnectConditionModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@ModuleType(ListableModuleDescriptorFactory.class)
@Component
public class ConnectConditionModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectConditionModuleDescriptor>{
    @Autowired
    public ConnectConditionModuleDescriptorFactory(@Qualifier("hostContainer") final HostContainer hostContainer)
    {
        super(hostContainer, "connect-conditions", ConnectConditionModuleDescriptor.class);
    }
}