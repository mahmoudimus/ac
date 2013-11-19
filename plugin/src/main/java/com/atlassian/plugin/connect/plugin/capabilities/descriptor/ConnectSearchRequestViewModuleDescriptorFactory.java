package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.SearchRequestViewCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

public class ConnectSearchRequestViewModuleDescriptorFactory implements ConnectModuleDescriptorFactory<SearchRequestViewCapabilityBean, SearchRequestViewModuleDescriptor>
{
    private final ConnectAutowireUtil autoWireUtil;

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
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
