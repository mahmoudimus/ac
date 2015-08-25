package com.atlassian.plugin.connect.jira.capabilities.descriptor;

import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.api.module.webitem.WebItemModuleDescriptorData;
import com.atlassian.plugin.connect.api.module.webitem.WebLinkFactory;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.spi.module.webitem.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;
import com.atlassian.uri.UriBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates JiraWebItemModuleDescriptor with link pointing to remote plugin.
 */
@JiraComponent
public class JiraWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory
{
    public static final ImmutableSet<String> ADMIN_MENUS_KEYS = ImmutableSet.of(
            "admin_system_menu",
            "admin_plugins_menu",
            "admin_users_menu",
            "admin_issues_menu",
            "admin_project_menu");

    private final WebInterfaceManager webInterfaceManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WebLinkFactory webLinkFactory;

    public static final String WEB_ITEM_SOURCE_QUERY_PARAM = "s";

    @Autowired
    public JiraWebItemModuleDescriptorFactory(
            WebInterfaceManager webInterfaceManager,
            JiraAuthenticationContext jiraAuthenticationContext,
            WebLinkFactory webLinkFactory)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.webInterfaceManager = webInterfaceManager;
        this.webLinkFactory = webLinkFactory;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(WebItemModuleDescriptorData webItemModuleDescriptorData)
    {
        WebItemModuleDescriptorData webItemModuleDescriptorDataWithValidUrl = appendSourceQueryParameterToUrlIfNeeded(webItemModuleDescriptorData);
        return new RemoteJiraWebItemModuleDescriptor(jiraAuthenticationContext, webInterfaceManager, webLinkFactory, webItemModuleDescriptorDataWithValidUrl);
    }

    private WebItemModuleDescriptorData appendSourceQueryParameterToUrlIfNeeded(WebItemModuleDescriptorData webItemModuleDescriptorData)
    {
        String section = webItemModuleDescriptorData.getSection();
        AddOnUrlContext addOnUrlContext = webItemModuleDescriptorData.getAddOnUrlContext();
        if (addOnUrlContext == AddOnUrlContext.page && isAdminSection(section))
        {
            String url = webItemModuleDescriptorData.getUrl();
            String moduleKey = webItemModuleDescriptorData.getModuleKey();
            String newUrl = addWebItemSourceQueryParamIfNotPresent(url, moduleKey);
            return WebItemModuleDescriptorData.builder(webItemModuleDescriptorData).setUrl(newUrl).build();
        }
        return webItemModuleDescriptorData;
    }

    private boolean isAdminSection(final String section)
    {
        String[] fragments = section.split("/");
        return fragments.length != 0 && ADMIN_MENUS_KEYS.contains(fragments[0]);
    }

    private String addWebItemSourceQueryParamIfNotPresent(String link, String key)
    {
        String path = getPathFromUrlTemplate(link);

        Map<String, List<String>> queryListMap = UriBuilder.splitParameters(getQueryFromUrlTemplate(link));

        HashMap<String, List<String>> resultingQueryMap = new HashMap<>(queryListMap);
        if (!resultingQueryMap.containsKey(WEB_ITEM_SOURCE_QUERY_PARAM))
        {
            resultingQueryMap.put(WEB_ITEM_SOURCE_QUERY_PARAM, ImmutableList.of(key));
        }

        return path + "?" + UriBuilder.joinParameters(resultingQueryMap);
    }

    private String getPathFromUrlTemplate(final String localUrl)
    {
        if (localUrl.indexOf('?') != -1)
        {
            return localUrl.substring(0, localUrl.indexOf('?'));
        }
        return localUrl;
    }

    private String getQueryFromUrlTemplate(final String localUrl)
    {
        if (localUrl.indexOf('?') != -1)
        {
            return localUrl.substring(localUrl.indexOf('?') + 1);
        }
        return "";
    }

    private static final class RemoteJiraWebItemModuleDescriptor extends JiraWebItemModuleDescriptor
    {
        private final WebLinkFactory webLinkFactory;
        private final WebItemModuleDescriptorData webItemModuleDescriptorData;

        public RemoteJiraWebItemModuleDescriptor(
                JiraAuthenticationContext jiraAuthenticationContext,
                WebInterfaceManager webInterfaceManager,
                WebLinkFactory webLinkFactory,
                WebItemModuleDescriptorData webItemModuleDescriptorData)
        {
            super(jiraAuthenticationContext, webInterfaceManager);
            this.webLinkFactory = webLinkFactory;
            this.webItemModuleDescriptorData = webItemModuleDescriptorData;
        }

        @Override
        public WebLink getLink()
        {
            WebLink remoteWebLink = webLinkFactory.createRemoteWebLink(this, webItemModuleDescriptorData);
            return new JiraWebLink(remoteWebLink, authenticationContext);
        }

        @Override
        public void destroy()
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
