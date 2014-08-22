package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
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
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WebFragmentModuleContextExtractor webFragmentModuleContextExtractor;
    private final ModuleContextFilter moduleContextFilter;

    @Autowired
    public JiraWebItemModuleDescriptorFactory(
            WebFragmentHelper webFragmentHelper,
            WebInterfaceManager webInterfaceManager,
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            JiraAuthenticationContext jiraAuthenticationContext,
            WebFragmentModuleContextExtractor webFragmentModuleContextExtractor,
            ModuleContextFilter moduleContextFilter,
            UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.webFragmentModuleContextExtractor = checkNotNull(webFragmentModuleContextExtractor);
        this.moduleContextFilter = checkNotNull(moduleContextFilter);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.iFrameUriBuilderFactory = checkNotNull(iFrameUriBuilderFactory);
        this.webInterfaceManager = checkNotNull(webInterfaceManager);
        this.webFragmentHelper = checkNotNull(webFragmentHelper);
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String pluginKey, String moduleKey, boolean absolute, AddOnUrlContext addOnUrlContext, boolean isDialog)
    {
        return new RemoteJiraWebItemModuleDescriptor(jiraAuthenticationContext, webInterfaceManager, webFragmentHelper,
                iFrameUriBuilderFactory, urlVariableSubstitutor, webFragmentModuleContextExtractor, moduleContextFilter,
                url, pluginKey, moduleKey, absolute, addOnUrlContext, isDialog);
    }

    private static final class RemoteJiraWebItemModuleDescriptor extends JiraWebItemModuleDescriptor
    {
        private final WebFragmentHelper webFragmentHelper;
        private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final WebFragmentModuleContextExtractor webFragmentModuleContextExtractor;
        private final ModuleContextFilter moduleContextFilter;
        private final String url;
        private final String pluginKey;
        private final String moduleKey;
        private boolean absolute;
        private final AddOnUrlContext addOnUrlContext;
        private final boolean isDialog;

        public RemoteJiraWebItemModuleDescriptor(
                JiraAuthenticationContext jiraAuthenticationContext,
                WebInterfaceManager webInterfaceManager,
                WebFragmentHelper webFragmentHelper,
                IFrameUriBuilderFactory iFrameUriBuilderFactory,
                UrlVariableSubstitutor urlVariableSubstitutor,
                WebFragmentModuleContextExtractor webFragmentModuleContextExtractor,
                ModuleContextFilter moduleContextFilter,
                String url,
                String pluginKey,
                String moduleKey,
                boolean absolute, AddOnUrlContext addOnUrlContext, boolean isDialog)
        {
            super(jiraAuthenticationContext, webInterfaceManager);
            this.webFragmentHelper = webFragmentHelper;
            this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
            this.moduleContextFilter = moduleContextFilter;
            this.url = url;
            this.pluginKey = pluginKey;
            this.moduleKey = moduleKey;
            this.absolute = absolute;
            this.addOnUrlContext = addOnUrlContext;
            this.isDialog = isDialog;
        }

        @Override
        public WebLink getLink()
        {
            return new JiraWebLink(new RemoteWebLink(this, webFragmentHelper, iFrameUriBuilderFactory,
                    urlVariableSubstitutor, webFragmentModuleContextExtractor, moduleContextFilter, url, pluginKey,
                    moduleKey, absolute, addOnUrlContext, isDialog), authenticationContext);
        }

        @Override
        public void destroy()
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
