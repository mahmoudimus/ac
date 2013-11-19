package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.SearchRequestViewCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.jira.searchrequestview.RemoteSearchRequestView;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class ConnectSearchRequestViewModuleDescriptorFactory implements ConnectModuleDescriptorFactory<SearchRequestViewCapabilityBean, SearchRequestViewModuleDescriptor>
{
    private final ConnectAutowireUtil autoWireUtil;

    @Autowired
    public ConnectSearchRequestViewModuleDescriptorFactory(ConnectAutowireUtil autowireUtil)
    {
        this.autoWireUtil = autowireUtil;
    }

    @Override
    public SearchRequestViewModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, SearchRequestViewCapabilityBean bean)
    {
        SearchRequestViewModuleDescriptorImpl descriptor = autoWireUtil.createBean(SearchRequestViewModuleDescriptorImpl.class);
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

        return element;
    }
}
