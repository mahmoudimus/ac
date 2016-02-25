package com.atlassian.plugin.connect.jira.search;

import java.net.URI;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebSearcherModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class WebSearcherModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebSearcherModuleBean, ConnectWebSearcherModuleDescriptor>
{
    private final ModuleFactory moduleFactory;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public WebSearcherModuleDescriptorFactory(final ModuleFactory moduleFactory, final ApplicationProperties applicationProperties)
    {
        this.moduleFactory = moduleFactory;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public ConnectWebSearcherModuleDescriptor createModuleDescriptor(final WebSearcherModuleBean bean, final ConnectAddonBean addon, final Plugin plugin)
    {
        ConnectWebSearcher webSearcher = new ConnectWebSearcher(bean.getDisplayName(), createUrl(addon, bean), bean.getKey(addon));
        ConnectWebSearcherModuleDescriptor descriptor = new ConnectWebSearcherModuleDescriptor(moduleFactory, webSearcher);
        Element xmlElement = new DOMElement("web-searcher");
        xmlElement.addAttribute("key", bean.getKey(addon));
        descriptor.init(plugin, xmlElement);
        return descriptor;
    }

    private URI createUrl(final ConnectAddonBean addon, final WebSearcherModuleBean bean)
    {
        return URI.create(applicationProperties.getBaseUrl(UrlMode.ABSOLUTE) + RedirectServletPath.forModule(addon.getKey(), bean.getRawKey()));
    }
}
