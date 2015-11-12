package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.connect.spi.web.ConnectWebFragmentLocationBlacklistModuleDescriptor;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@ModuleType (ListableModuleDescriptorFactory.class)
@Component
public final class ConnectContextParameterResolverModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectWebFragmentLocationBlacklistModuleDescriptor>
{
    @Autowired
    public ConnectContextParameterResolverModuleDescriptorFactory(@Qualifier ("hostContainer") final HostContainer hostContainer)
    {
        super(hostContainer, "connect-context-parameters-resolver", ConnectWebFragmentLocationBlacklistModuleDescriptor.class);
    }
}