package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebPanelModuleContextExtractor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
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
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WebPanelModuleContextExtractor webPanelModuleContextExtractor;
    private final ModuleContextFilter moduleContextFilter;

    @Autowired
    public JiraWebItemModuleDescriptorFactory(
            WebFragmentHelper webFragmentHelper,
            WebInterfaceManager webInterfaceManager,
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            JiraAuthenticationContext jiraAuthenticationContext, 
            WebPanelModuleContextExtractor webPanelModuleContextExtractor, 
            ModuleContextFilter moduleContextFilter)
    {
        this.webPanelModuleContextExtractor = checkNotNull(webPanelModuleContextExtractor);
        this.moduleContextFilter = checkNotNull(moduleContextFilter);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.iFrameUriBuilderFactory = checkNotNull(iFrameUriBuilderFactory);
        this.webInterfaceManager = checkNotNull(webInterfaceManager);
        this.webFragmentHelper = checkNotNull(webFragmentHelper);
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(
            String url
            , String pluginKey
            , String moduleKey
            , boolean absolute
            , AddOnUrlContext addOnUrlContext)
    {
        return new RemoteJiraWebItemModuleDescriptor(jiraAuthenticationContext, webInterfaceManager, webFragmentHelper,
                iFrameUriBuilderFactory, webPanelModuleContextExtractor, moduleContextFilter, url, pluginKey, moduleKey, absolute, addOnUrlContext);
    }

    private static final class RemoteJiraWebItemModuleDescriptor extends JiraWebItemModuleDescriptor
    {
        private final WebFragmentHelper webFragmentHelper;
        private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
        private final WebPanelModuleContextExtractor webPanelModuleContextExtractor;
        private final ModuleContextFilter moduleContextFilter;
        private final String url;
        private final String pluginKey;
        private final String moduleKey;
        private boolean absolute;
        private final AddOnUrlContext addOnUrlContext;

        public RemoteJiraWebItemModuleDescriptor(
                JiraAuthenticationContext jiraAuthenticationContext,
                WebInterfaceManager webInterfaceManager,
                WebFragmentHelper webFragmentHelper,
                IFrameUriBuilderFactory iFrameUriBuilderFactory,
                WebPanelModuleContextExtractor webPanelModuleContextExtractor, 
                ModuleContextFilter moduleContextFilter, 
                String url,
                String moduleKey,
                String pluginKey,
                boolean absolute, AddOnUrlContext addOnUrlContext)
        {
            super(jiraAuthenticationContext, webInterfaceManager);
            this.webFragmentHelper = webFragmentHelper;
            this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
            this.webPanelModuleContextExtractor = webPanelModuleContextExtractor;
            this.moduleContextFilter = moduleContextFilter;
            this.url = url;
            this.pluginKey = pluginKey;
            this.moduleKey = moduleKey;
            this.absolute = absolute;
            this.addOnUrlContext = addOnUrlContext;
        }

        @Override
        public WebLink getLink()
        {
            return new JiraWebLink(new RemoteWebLink(this, webFragmentHelper, iFrameUriBuilderFactory, webPanelModuleContextExtractor, moduleContextFilter, url, pluginKey, moduleKey, absolute, addOnUrlContext), authenticationContext);
        }

        @Override
        public void destroy()
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
