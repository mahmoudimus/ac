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
import com.atlassian.plugin.connect.plugin.module.jira.searchrequestview.RemoteSearchRequestView;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URISyntaxException;

@JiraComponent
public class SearchRequestViewModuleDescriptorFactory implements ConnectModuleDescriptorFactory<SearchRequestViewCapabilityBean, SearchRequestViewModuleDescriptor>
{
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestURLHandler urlHandler;
    private final ConditionDescriptorFactory conditionDescriptorFactory;
    private ApplicationProperties applicationProperties;
    private SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private TemplateRenderer templateRenderer;
    private RemotablePluginAccessor remotablePluginAccessor;

    @Autowired
    public SearchRequestViewModuleDescriptorFactory(JiraAuthenticationContext authenticationContext,
                                                    SearchRequestURLHandler urlHandler,
                                                    ConditionDescriptorFactory conditionDescriptorFactory,
                                                    ApplicationProperties applicationProperties,
                                                    SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
                                                    TemplateRenderer templateRenderer,
                                                    RemotablePluginAccessor remotablePluginAccessor)
    {
        this.authenticationContext = checkNotNull(authenticationContext);
        this.urlHandler = checkNotNull(urlHandler);
        this.conditionDescriptorFactory = checkNotNull(conditionDescriptorFactory);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.searchRequestViewBodyWriterUtil = checkNotNull(searchRequestViewBodyWriterUtil);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.remotablePluginAccessor = checkNotNull(remotablePluginAccessor);
    }

    @Override
    public SearchRequestViewModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, SearchRequestViewCapabilityBean bean)
    {
        SearchRequestViewModuleDescriptorImpl descriptor = new SearchRequestViewModuleDescriptorImpl(authenticationContext,
                urlHandler, createModuleFactory(bean), conditionDescriptorFactory);
        Element element = createElement(bean);
        descriptor.init(plugin, element);
        return descriptor;
    }


    private Element createElement(SearchRequestViewCapabilityBean bean)
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

        return element;
    }

    private ModuleFactory createModuleFactory(final SearchRequestViewCapabilityBean bean)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                try
                {
                    return (T) new RemoteSearchRequestView(applicationProperties, searchRequestViewBodyWriterUtil,
                            templateRenderer, bean.createUri(), bean.getDisplayName(), remotablePluginAccessor);
                }
                catch (URISyntaxException e)
                {
                    throw new PluginParseException(e);
                }
            }
        };
    }

}
