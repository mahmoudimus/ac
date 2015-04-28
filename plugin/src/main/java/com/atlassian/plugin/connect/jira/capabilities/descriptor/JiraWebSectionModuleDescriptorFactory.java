package com.atlassian.plugin.connect.jira.capabilities.descriptor;

import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebSectionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.plugin.module.websection.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

@JiraComponent
public class JiraWebSectionModuleDescriptorFactory implements ProductSpecificWebSectionModuleDescriptorFactory
{
    private final WebInterfaceManager webInterfaceManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    @Autowired
    public JiraWebSectionModuleDescriptorFactory(WebInterfaceManager webInterfaceManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.webInterfaceManager = checkNotNull(webInterfaceManager);
    }

    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor()
    {
        return new JiraWebSectionModuleDescriptor(jiraAuthenticationContext, webInterfaceManager);
    }
}
