package com.atlassian.plugin.connect.jira.search;

import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.jira.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

@JiraComponent
public class SearchRequestViewModuleDescriptorFactory implements ConnectModuleDescriptorFactory<SearchRequestViewModuleBean, SearchRequestViewModuleDescriptor> {
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestURLHandler urlHandler;
    private final ConnectConditionDescriptorFactory conditionDescriptorFactory;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ApplicationProperties applicationProperties;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final TemplateRenderer templateRenderer;
    private final ConnectUriFactory connectUriFactory;

    @Autowired
    public SearchRequestViewModuleDescriptorFactory(JiraAuthenticationContext authenticationContext,
                                                    ConditionModuleFragmentFactory conditionModuleFragmentFactory,
                                                    ConnectConditionDescriptorFactory conditionDescriptorFactory,
                                                    ApplicationProperties applicationProperties,
                                                    SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
                                                    TemplateRenderer templateRenderer,
                                                    ConnectUriFactory connectUriFactory,
                                                    DelegatingComponentAccessor componentAccessor) {
        this.authenticationContext = checkNotNull(authenticationContext);
        this.urlHandler = checkNotNull(componentAccessor.getComponent(SearchRequestURLHandler.class));
        this.conditionDescriptorFactory = checkNotNull(conditionDescriptorFactory);
        this.conditionModuleFragmentFactory = checkNotNull(conditionModuleFragmentFactory);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.searchRequestViewBodyWriterUtil = checkNotNull(searchRequestViewBodyWriterUtil);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.connectUriFactory = checkNotNull(connectUriFactory);
    }

    @Override
    public SearchRequestViewModuleDescriptor createModuleDescriptor(SearchRequestViewModuleBean bean, ConnectAddonBean addon, Plugin plugin) {
        SearchRequestViewModuleDescriptorImpl descriptor = new SearchRequestViewModuleDescriptorImpl(authenticationContext,
                urlHandler, createModuleFactory(bean, addon), conditionDescriptorFactory);
        Element element = createElement(bean, addon);
        descriptor.init(plugin, element);
        return descriptor;
    }


    private Element createElement(SearchRequestViewModuleBean bean, ConnectAddonBean addon) {
        DOMElement element = new DOMElement("search-request-view");

        element.setAttribute("key", bean.getKey(addon));
        element.setAttribute("name", bean.getDisplayName());
        element.setAttribute("class", RemoteSearchRequestView.class.getName());
        element.setAttribute("state", "enabled");

        // The extension and content type refer to the HTML fragment that will be rendered
        // by {@link RemoteSearchRequestView}, not to the format that the add-on generates.
        element.setAttribute("fileExtension", "html");
        element.setAttribute("contentType", "text/html");

        element.addElement("order")
                .addText(Integer.toString(bean.getWeight()));
        element.addElement("description")
                .addText(bean.getDescription().getValue())
                .addAttribute("key", bean.getDescription().getI18n());

        if (!bean.getConditions().isEmpty()) {
            element.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), bean.getConditions()));
        } else {
            // JIRA throws an NPE if no conditions are present...
            element.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), Collections.<ConditionalBean>emptyList(),
                    Collections.<Class<? extends Condition>>singletonList(AlwaysDisplayCondition.class)));
        }

        return element;
    }

    private ModuleFactory createModuleFactory(final SearchRequestViewModuleBean bean, final ConnectAddonBean addon) {
        return new ModuleFactory() {

            @Override
            @SuppressWarnings("unchecked")
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException {
                try {
                    return (T) new RemoteSearchRequestView(
                            applicationProperties,
                            searchRequestViewBodyWriterUtil,
                            templateRenderer,
                            connectUriFactory,
                            addon.getKey(),
                            bean.getKey(addon),
                            bean.createUri(),
                            bean.getDisplayName(),
                            authenticationContext);
                } catch (URISyntaxException e) {
                    throw new PluginParseException(e);
                }
            }
        };
    }

}
