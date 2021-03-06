package com.atlassian.plugin.connect.jira.web;

import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.RemoteWebLink;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.web.WebFragmentHelper;
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
public class JiraWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory {
    public static final ImmutableSet<String> ADMIN_MENUS_KEYS = ImmutableSet.of(
            "admin_system_menu",
            "admin_plugins_menu",
            "admin_users_menu",
            "admin_issues_menu",
            "admin_project_menu");

    private final WebFragmentHelper webFragmentHelper;
    private final WebInterfaceManager webInterfaceManager;
    private final ConnectUriFactory connectUriFactory;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;
    private final ModuleContextFilter moduleContextFilter;

    public static final String WEB_ITEM_SOURCE_QUERY_PARAM = "s";

    @Autowired
    public JiraWebItemModuleDescriptorFactory(
            WebFragmentHelper webFragmentHelper,
            WebInterfaceManager webInterfaceManager,
            ConnectUriFactory connectUriFactory,
            JiraAuthenticationContext jiraAuthenticationContext,
            PluggableParametersExtractor webFragmentModuleContextExtractor,
            ModuleContextFilter moduleContextFilter,
            UrlVariableSubstitutor urlVariableSubstitutor) {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.webFragmentModuleContextExtractor = checkNotNull(webFragmentModuleContextExtractor);
        this.moduleContextFilter = checkNotNull(moduleContextFilter);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.connectUriFactory = checkNotNull(connectUriFactory);
        this.webInterfaceManager = checkNotNull(webInterfaceManager);
        this.webFragmentHelper = checkNotNull(webFragmentHelper);
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String pluginKey, String moduleKey, boolean absolute, AddonUrlContext addonUrlContext, boolean isDialog, String section) {
        return new RemoteJiraWebItemModuleDescriptor(jiraAuthenticationContext, webInterfaceManager, webFragmentHelper,
                connectUriFactory, urlVariableSubstitutor, webFragmentModuleContextExtractor, moduleContextFilter,
                url, pluginKey, moduleKey, absolute, addonUrlContext, isDialog, section);
    }

    private static final class RemoteJiraWebItemModuleDescriptor extends JiraWebItemModuleDescriptor {
        private final WebFragmentHelper webFragmentHelper;
        private final ConnectUriFactory connectUriFactory;
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final PluggableParametersExtractor webFragmentModuleContextExtractor;
        private final ModuleContextFilter moduleContextFilter;
        private final String url;
        private final String pluginKey;
        private final String moduleKey;
        private boolean absolute;
        private final AddonUrlContext addonUrlContext;
        private final boolean isDialog;
        private final String section;

        public RemoteJiraWebItemModuleDescriptor(
                JiraAuthenticationContext jiraAuthenticationContext,
                WebInterfaceManager webInterfaceManager,
                WebFragmentHelper webFragmentHelper,
                ConnectUriFactory connectUriFactory,
                UrlVariableSubstitutor urlVariableSubstitutor,
                PluggableParametersExtractor webFragmentModuleContextExtractor,
                ModuleContextFilter moduleContextFilter,
                String url,
                String pluginKey,
                String moduleKey,
                boolean absolute, AddonUrlContext addonUrlContext, boolean isDialog, String section) {
            super(jiraAuthenticationContext, webInterfaceManager);
            this.webFragmentHelper = webFragmentHelper;
            this.connectUriFactory = connectUriFactory;
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
            this.moduleContextFilter = moduleContextFilter;
            this.pluginKey = pluginKey;
            this.moduleKey = moduleKey;
            this.absolute = absolute;
            this.addonUrlContext = addonUrlContext;
            this.isDialog = isDialog;
            this.section = section;

            this.url = appendSourceQueryParameterToUrlIfNeeded(url);
        }

        private String appendSourceQueryParameterToUrlIfNeeded(final String url) {
            if (addonUrlContext == AddonUrlContext.page && isAdminSection(section)) {
                return addWebItemSourceQueryParamIfNotPresent(url, moduleKey);
            }
            return url;
        }

        @Override
        public WebLink getLink() {
            return new JiraWebLink(new RemoteWebLink(this, webFragmentHelper, connectUriFactory,
                    urlVariableSubstitutor, webFragmentModuleContextExtractor, moduleContextFilter, url, pluginKey,
                    moduleKey, absolute, addonUrlContext, isDialog), authenticationContext);
        }

        @Override
        public void destroy() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        private String addWebItemSourceQueryParamIfNotPresent(String link, String key) {
            String path = getPathFromUrlTemplate(link);

            Map<String, List<String>> queryListMap = UriBuilder.splitParameters(getQueryFromUrlTemplate(link));

            HashMap<String, List<String>> resultingQueryMap = new HashMap<>(queryListMap);
            if (!resultingQueryMap.containsKey(WEB_ITEM_SOURCE_QUERY_PARAM)) {
                resultingQueryMap.put(WEB_ITEM_SOURCE_QUERY_PARAM, ImmutableList.of(key));
            }

            return path + "?" + UriBuilder.joinParameters(resultingQueryMap);
        }

        private boolean isAdminSection(final String section) {
            String[] fragments = section.split("/");
            return fragments.length != 0 && ADMIN_MENUS_KEYS.contains(fragments[0]);
        }

        private String getQueryFromUrlTemplate(final String localUrl) {
            if (localUrl.indexOf('?') != -1) {
                return localUrl.substring(localUrl.indexOf('?') + 1);
            }
            return "";
        }

        private String getPathFromUrlTemplate(final String localUrl) {
            if (localUrl.indexOf('?') != -1) {
                return localUrl.substring(0, localUrl.indexOf('?'));
            }
            return localUrl;
        }
    }
}
