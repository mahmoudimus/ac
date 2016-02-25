package com.atlassian.plugin.connect.jira.search;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.redirect.RedirectData;
import com.atlassian.plugin.connect.api.web.redirect.RedirectDataBuilderFactory;
import com.atlassian.plugin.connect.api.web.redirect.RedirectRegistry;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebSearcherModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSearcherModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.api.web.redirect.RedirectData.AccessDeniedTemplateType.IFRAME;
import static com.atlassian.plugin.connect.api.web.redirect.RedirectData.AccessDeniedTemplateType.PAGE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@JiraComponent
public class WebSearcherModuleProvider extends AbstractJiraConnectModuleProvider<WebSearcherModuleBean>
{
    private static final WebSearcherModuleMeta META = new WebSearcherModuleMeta();

    private final WebSearcherModuleDescriptorFactory webSearcherModuleDescriptorFactory;
    private final RedirectRegistry redirectRegistry;
    private final RedirectDataBuilderFactory redirectDataBuilderFactory;

    @Autowired
    public WebSearcherModuleProvider(final PluginRetrievalService pluginRetrievalService, final ConnectJsonSchemaValidator schemaValidator, final WebSearcherModuleDescriptorFactory webSearcherModuleDescriptorFactory, final RedirectRegistry redirectRegistry, final RedirectDataBuilderFactory redirectDataBuilderFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.webSearcherModuleDescriptorFactory = webSearcherModuleDescriptorFactory;
        this.redirectRegistry = redirectRegistry;
        this.redirectDataBuilderFactory = redirectDataBuilderFactory;
    }

    @Override
    public ConnectModuleMeta<WebSearcherModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(final List<WebSearcherModuleBean> modules, final ConnectAddonBean addon)
    {
        Plugin plugin = pluginRetrievalService.getPlugin();
        return modules.stream()
                .map(bean -> {
                    registerRedirection(bean, addon);
                    return webSearcherModuleDescriptorFactory.createModuleDescriptor(bean, addon, plugin);
                })
                .collect(toList());
    }

    private void registerRedirection(final WebSearcherModuleBean bean, final ConnectAddonBean addon)
    {
        RedirectData redirectData = redirectDataBuilderFactory.builder()
                .addOn(addon.getKey())
                .urlTemplate(bean.getUrl())
                .accessDeniedTemplateType(PAGE)
                .title(bean.getDisplayName())
                .conditions(emptyList())
                .build();

        redirectRegistry.register(addon.getKey(), bean.getRawKey(), redirectData);
    }
}
