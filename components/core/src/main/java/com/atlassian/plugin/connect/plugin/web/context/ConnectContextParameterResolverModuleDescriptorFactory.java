package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ModuleType (ListableModuleDescriptorFactory.class)
@Component
public final class ConnectContextParameterResolverModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectContextParameterResolverModuleDescriptor>
{

    private static final String TYPE = "connect-context-parameters-resolver";

    @Autowired
    public ConnectContextParameterResolverModuleDescriptorFactory(HostContainer hostContainer)
    {
        super(hostContainer, TYPE, ConnectContextParameterResolverModuleDescriptor.class);
    }
}