package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.spring.JiraComponent;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;

import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates JiraWebItemModuleDescriptor with link pointing to remote plugin.
 */
@JiraComponent
public class JiraWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory
{
    private final WebFragmentHelper webFragmentHelper;
    private final WebInterfaceManager webInterfaceManager;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer contextMapURLSerializer;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    @Autowired
    public JiraWebItemModuleDescriptorFactory(
            WebFragmentHelper webFragmentHelper,
            WebInterfaceManager webInterfaceManager,
            UrlVariableSubstitutor urlVariableSubstitutor,
            ContextMapURLSerializer contextMapURLSerializer,
            JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.contextMapURLSerializer = checkNotNull(contextMapURLSerializer);
        this.webInterfaceManager = checkNotNull(webInterfaceManager);
        this.webFragmentHelper = checkNotNull(webFragmentHelper);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);

    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String linkId, boolean absolute)
    {
        return new RemoteJiraWebItemModuleDescriptor(jiraAuthenticationContext, webInterfaceManager, webFragmentHelper, urlVariableSubstitutor, contextMapURLSerializer, url, linkId, absolute);
    }

    private static final class RemoteJiraWebItemModuleDescriptor extends JiraWebItemModuleDescriptor
    {
        private final WebFragmentHelper webFragmentHelper;
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final ContextMapURLSerializer contextMapURLSerializer;
        private final String url;
        private final String linkId;
        private boolean absolute;

        public RemoteJiraWebItemModuleDescriptor(
                JiraAuthenticationContext jiraAuthenticationContext,
                WebInterfaceManager webInterfaceManager,
                WebFragmentHelper webFragmentHelper,
                UrlVariableSubstitutor urlVariableSubstitutor,
                ContextMapURLSerializer contextMapURLSerializer,
                String url,
                String linkId,
                boolean absolute)
        {
            super(jiraAuthenticationContext, webInterfaceManager);
            this.webFragmentHelper = webFragmentHelper;
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.contextMapURLSerializer = contextMapURLSerializer;
            this.url = url;
            this.linkId = linkId;
            this.absolute = absolute;
        }
        @Override
        public WebLink getLink()
        {
            return new JiraWebLink(new RemoteWebLink(this, webFragmentHelper, urlVariableSubstitutor, contextMapURLSerializer, url, linkId, absolute), authenticationContext);
        }
    }
}
