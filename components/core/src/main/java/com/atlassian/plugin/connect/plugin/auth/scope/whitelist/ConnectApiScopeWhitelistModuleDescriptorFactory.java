package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@ModuleType(ListableModuleDescriptorFactory.class)
@Component
public class ConnectApiScopeWhitelistModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConnectApiScopeWhitelistModuleDescriptor>
{

    private static final String TYPE = "connect-api-scope-whitelist";

    @Autowired
    public ConnectApiScopeWhitelistModuleDescriptorFactory(@Qualifier("hostContainer") final HostContainer hostContainer)
    {
        super(hostContainer, TYPE, ConnectApiScopeWhitelistModuleDescriptor.class);
    }
}
