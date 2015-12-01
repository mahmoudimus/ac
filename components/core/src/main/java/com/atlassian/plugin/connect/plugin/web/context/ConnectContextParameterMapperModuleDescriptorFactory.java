package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ModuleType(ListableModuleDescriptorFactory.class)
@Component
public class ConnectContextParameterMapperModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectContextParameterMapperModuleDescriptor>
{

    private static final String TYPE = "connect-context-parameter-mapper";

    @Autowired
    public ConnectContextParameterMapperModuleDescriptorFactory(HostContainer hostContainer)
    {
        super(hostContainer, TYPE, ConnectContextParameterMapperModuleDescriptor.class);
    }
}
