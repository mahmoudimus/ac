package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptorFactory;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPageBean;

//@Component
public class ConnectIssueTabPanelModuleProvider implements ConnectModuleProvider<ConnectIssueTabPanelCapabilityBean>
{
    private final ConnectIssueTabPanelModuleDescriptorFactory issueTabFactory;

//    @Autowired
    public ConnectIssueTabPanelModuleProvider(ConnectIssueTabPanelModuleDescriptorFactory issueTabFactory)
    {
        this.issueTabFactory = issueTabFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<ConnectIssueTabPanelCapabilityBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (ConnectIssueTabPanelCapabilityBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin,addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, ConnectIssueTabPanelCapabilityBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        ConnectIssueTabPanelCapabilityBean newBean = newIssueTabPageBean(bean).withUrl(bean.getUrl()).build();
        descriptors.add(issueTabFactory.createModuleDescriptor(plugin, addonBundleContext, newBean));

        return descriptors;
    }
}
