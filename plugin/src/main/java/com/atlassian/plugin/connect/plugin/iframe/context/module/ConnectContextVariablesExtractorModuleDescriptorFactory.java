package com.atlassian.plugin.connect.plugin.iframe.context.module;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@ModuleType (ListableModuleDescriptorFactory.class)
@Component
public final class ConnectContextVariablesExtractorModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectContextVariablesExtractorModuleDescriptor>
{
    @Autowired
    public ConnectContextVariablesExtractorModuleDescriptorFactory(@Qualifier ("hostContainer") final HostContainer hostContainer)
    {
        super(hostContainer, "connect-context-parameters-extractor", ConnectContextVariablesExtractorModuleDescriptor.class);
    }
}