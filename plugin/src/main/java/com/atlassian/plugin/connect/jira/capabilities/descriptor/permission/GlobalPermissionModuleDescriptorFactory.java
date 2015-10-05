package com.atlassian.plugin.connect.jira.capabilities.descriptor.permission;

import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptor;
import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptorImpl;
import com.atlassian.jira.plugin.permission.GlobalPermissionModuleDescriptor;
import com.atlassian.jira.plugin.permission.GlobalPermissionModuleDescriptorImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class GlobalPermissionModuleDescriptorFactory  implements ConnectModuleDescriptorFactory<GlobalPermissionModuleBean, GlobalPermissionModuleDescriptor>
{
    public static final String DESCRIPTOR_NAME = "global-permission";

    private final ConnectContainerUtil autowireUtil;

    @Autowired
    public GlobalPermissionModuleDescriptorFactory(ConnectContainerUtil autowireUtil)
    {
        this.autowireUtil = autowireUtil;
    }

    @Override
    public GlobalPermissionModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, GlobalPermissionModuleBean bean)
    {
        Element globalPermission = new DOMElement(DESCRIPTOR_NAME);

        ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        globalPermission.addAttribute("key", bean.getKey(connectAddonBean));
        globalPermission.addAttribute("i18n-name-key", bean.getName().getI18nOrValue());
        globalPermission.addAttribute("i18n-description-key", bean.getDescription().getI18nOrValue());
        globalPermission.addAttribute("anonymous-allowed", bean.getAnonymousAllowed().toString());

        GlobalPermissionModuleDescriptorImpl descriptor = autowireUtil.createBean(GlobalPermissionModuleDescriptorImpl.class);
        descriptor.init(theConnectPlugin, globalPermission);

        return descriptor;
    }

}
