package com.atlassian.plugin.connect.plugin.iframe.context.module;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;

import javax.inject.Inject;
import javax.inject.Named;

@ModuleType (ListableModuleDescriptorFactory.class)
@Named ("connectContextVariablesValidatorModuleDescriptorFactory")
public final class ConnectContextVariablesValidatorModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectContextVariablesValidatorModuleDescriptor>
{
    @Inject
    public ConnectContextVariablesValidatorModuleDescriptorFactory(final HostContainer hostContainer)
    {
        super(hostContainer, "connect-context-variables-validator", ConnectContextVariablesValidatorModuleDescriptor.class);
    }
}
