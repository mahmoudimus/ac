package com.atlassian.plugin.connect.jira.permission;

import com.atlassian.jira.plugin.permission.GlobalPermissionModuleDescriptor;
import com.atlassian.jira.plugin.permission.GlobalPermissionModuleDescriptorImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class GlobalPermissionModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<GlobalPermissionModuleBean, GlobalPermissionModuleDescriptor>
{
    public static final String DESCRIPTOR_NAME = "global-permission";

    private final ConnectContainerUtil autowireUtil;

    @Autowired
    public GlobalPermissionModuleDescriptorFactory(ConnectContainerUtil autowireUtil)
    {
        this.autowireUtil = autowireUtil;
    }

    @Override
    public GlobalPermissionModuleDescriptor createModuleDescriptor(GlobalPermissionModuleBean bean, ConnectAddonBean addon, Plugin plugin)
    {
        Element globalPermission = new DOMElement(DESCRIPTOR_NAME);

        globalPermission.addAttribute("key", bean.getKey(addon));
        globalPermission.addAttribute("i18n-name-key", bean.getName().getI18nOrValue());
        globalPermission.addAttribute("i18n-description-key", bean.getDescription().getI18nOrValue());
        globalPermission.addAttribute("anonymous-allowed", bean.getAnonymousAllowed().toString());

        GlobalPermissionModuleDescriptorImpl descriptor = autowireUtil.createBean(GlobalPermissionModuleDescriptorImpl.class);
        descriptor.init(plugin, globalPermission);

        return descriptor;
    }
}
