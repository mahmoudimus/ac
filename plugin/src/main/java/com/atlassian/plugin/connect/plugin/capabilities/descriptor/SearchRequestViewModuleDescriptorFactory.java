package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.module.jira.searchrequestview.ConnectConditionDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.jira.searchrequestview.RemoteSearchRequestView;
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
public class SearchRequestViewModuleDescriptorFactory implements ConnectModuleDescriptorFactory<SearchRequestViewModuleBean, SearchRequestViewModuleDescriptor>
{
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestURLHandler urlHandler;
    private final ConnectConditionDescriptorFactory conditionDescriptorFactory;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ApplicationProperties applicationProperties;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final TemplateRenderer templateRenderer;
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;

    @Autowired
    public SearchRequestViewModuleDescriptorFactory(JiraAuthenticationContext authenticationContext,
                                                    ConditionModuleFragmentFactory conditionModuleFragmentFactory,
                                                    ConnectConditionDescriptorFactory conditionDescriptorFactory,
                                                    ApplicationProperties applicationProperties,
                                                    SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
                                                    TemplateRenderer templateRenderer,
                                                    IFrameUriBuilderFactory iFrameUriBuilderFactory,
                                                    DelegatingComponentAccessor componentAccessor)
    {
        this.authenticationContext = checkNotNull(authenticationContext);
        this.urlHandler = checkNotNull(componentAccessor.getComponent(SearchRequestURLHandler.class));
        this.conditionDescriptorFactory = checkNotNull(conditionDescriptorFactory);
        this.conditionModuleFragmentFactory = checkNotNull(conditionModuleFragmentFactory);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.searchRequestViewBodyWriterUtil = checkNotNull(searchRequestViewBodyWriterUtil);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.iFrameUriBuilderFactory = checkNotNull(iFrameUriBuilderFactory);
    }

    @Override
    public SearchRequestViewModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, SearchRequestViewModuleBean bean)
    {
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        SearchRequestViewModuleDescriptorImpl descriptor = new SearchRequestViewModuleDescriptorImpl(authenticationContext,
                urlHandler, createModuleFactory(bean, connectAddonBean), conditionDescriptorFactory);
        Element element = createElement(bean, connectAddonBean);
        descriptor.init(theConnectPlugin, element);
        return descriptor;
    }


    private Element createElement(SearchRequestViewModuleBean bean, ConnectAddonBean addon)
    {
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

        if (!bean.getConditions().isEmpty())
        {
            element.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), bean.getConditions()));
        } else {
            // JIRA throws an NPE if no conditions are present...
            element.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), Collections.<ConditionalBean>emptyList(),
                    Collections.<Class<? extends Condition>>singletonList(AlwaysDisplayCondition.class)));
        }

        return element;
    }

    private ModuleFactory createModuleFactory(final SearchRequestViewModuleBean bean, final ConnectAddonBean addon)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                try
                {
                    return (T) new RemoteSearchRequestView(
                            applicationProperties,
                            searchRequestViewBodyWriterUtil,
                            templateRenderer,
                            iFrameUriBuilderFactory,
                            addon.getKey(),
                            bean.getKey(addon),
                            bean.createUri(),
                            bean.getDisplayName());
                }
                catch (URISyntaxException e)
                {
                    throw new PluginParseException(e);
                }
            }
        };
    }

}
