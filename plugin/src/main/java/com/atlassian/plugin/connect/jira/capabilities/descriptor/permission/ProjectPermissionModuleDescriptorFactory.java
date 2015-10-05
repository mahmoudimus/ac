package com.atlassian.plugin.connect.jira.capabilities.descriptor.permission;

import com.atlassian.jira.plugin.permission.ProjectPermissionModuleDescriptor;
import com.atlassian.jira.plugin.permission.ProjectPermissionModuleDescriptorImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class ProjectPermissionModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<ProjectPermissionModuleBean, ProjectPermissionModuleDescriptor>
{
    public static final String DESCRIPTOR_NAME = "project-permission";

    private final ConnectContainerUtil autowireUtil;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;

    @Autowired
    public ProjectPermissionModuleDescriptorFactory(ConnectContainerUtil autowireUtil,
            ConditionModuleFragmentFactory conditionModuleFragmentFactory)
    {
        this.autowireUtil = autowireUtil;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
    }

    @Override
    public ProjectPermissionModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, ProjectPermissionModuleBean bean)
    {
        ConnectAddonBean addon = moduleProviderContext.getConnectAddonBean();
        Element projectPermissionElement = new DOMElement(DESCRIPTOR_NAME);

        ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        projectPermissionElement.addAttribute("key", bean.getKey(connectAddonBean));
        projectPermissionElement.addAttribute("i18n-name-key", bean.getName().getI18nOrValue());
        projectPermissionElement.addAttribute("i18n-description-key", bean.getDescription().getI18nOrValue());
        projectPermissionElement.addAttribute("category", bean.getCategory().toString());
        if (!bean.getConditions().isEmpty()) {
            projectPermissionElement.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), bean.getConditions()));
        }

        ProjectPermissionModuleDescriptor descriptor = autowireUtil.createBean(ProjectPermissionModuleDescriptorImpl.class);
        descriptor.init(theConnectPlugin, projectPermissionElement);

        return descriptor;
    }
}
