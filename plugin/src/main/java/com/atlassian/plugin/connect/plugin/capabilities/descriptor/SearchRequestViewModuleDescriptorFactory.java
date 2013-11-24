package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.SearchRequestViewCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.module.jira.searchrequestview.RemoteSearchRequestView;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

@JiraComponent
public class SearchRequestViewModuleDescriptorFactory implements ConnectModuleDescriptorFactory<SearchRequestViewCapabilityBean, SearchRequestViewModuleDescriptor>
{
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestURLHandler urlHandler;
    private final ConditionDescriptorFactory conditionDescriptorFactory;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private ApplicationProperties applicationProperties;
    private SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private TemplateRenderer templateRenderer;
    private RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Autowired
    public SearchRequestViewModuleDescriptorFactory(JiraAuthenticationContext authenticationContext,
                                                    ConditionModuleFragmentFactory conditionModuleFragmentFactory,
                                                    ApplicationProperties applicationProperties,
                                                    SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
                                                    TemplateRenderer templateRenderer,
                                                    RemotablePluginAccessorFactory remotablePluginAccessorFactory,
                                                    DelegatingComponentAccessor componentAccessor)
    {
        this.authenticationContext = checkNotNull(authenticationContext);
        this.urlHandler = checkNotNull(componentAccessor.getComponent(SearchRequestURLHandler.class));
        this.conditionDescriptorFactory = checkNotNull(componentAccessor.getComponent(ConditionDescriptorFactory.class));
        this.conditionModuleFragmentFactory = checkNotNull(conditionModuleFragmentFactory);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.searchRequestViewBodyWriterUtil = checkNotNull(searchRequestViewBodyWriterUtil);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
    }

    @Override
    public SearchRequestViewModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, SearchRequestViewCapabilityBean bean)
    {
        SearchRequestViewModuleDescriptorImpl descriptor = new SearchRequestViewModuleDescriptorImpl(authenticationContext,
                urlHandler, createModuleFactory(bean, plugin), conditionDescriptorFactory);
        Element element = createElement(bean, plugin);
        descriptor.init(plugin, element);
        return descriptor;
    }


    private Element createElement(SearchRequestViewCapabilityBean bean, Plugin plugin)
    {
        DOMElement element = new DOMElement("search-request-view");

        element.setAttribute("key", bean.getKey());
        element.setAttribute("name", bean.getDisplayName());
        element.setAttribute("class", RemoteSearchRequestView.class.getName());
        element.setAttribute("state", "enabled");
        element.setAttribute("fileExtension", "html");
        element.setAttribute("contentType", "text/html");
        element.addElement("order")
                .addText(Integer.toString(bean.getWeight()));
        element.addElement("description")
                .addText(bean.getDescription().getValue())
                .addAttribute("key", bean.getDescription().getI18n());

        if (!bean.getConditions().isEmpty())
        {
            element.add(conditionModuleFragmentFactory.createFragment(plugin.getKey(), bean.getConditions()));
        }

        return element;
    }

    private ModuleFactory createModuleFactory(final SearchRequestViewCapabilityBean bean, final Plugin plugin)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                try
                {
                    return (T) new RemoteSearchRequestView(applicationProperties, searchRequestViewBodyWriterUtil,
                            templateRenderer, bean.createUri(), bean.getDisplayName(), remotablePluginAccessorFactory.get(plugin.getKey()));
                }
                catch (URISyntaxException e)
                {
                    throw new PluginParseException(e);
                }
            }
        };
    }

}
