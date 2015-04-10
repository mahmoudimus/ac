package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.fugue.Option;
import com.atlassian.gadgets.directory.Category;
import com.atlassian.gadgets.plugins.DashboardItemModule.Author;
import com.atlassian.gadgets.plugins.DashboardItemModule.DirectoryDefinition;
import com.atlassian.gadgets.plugins.DashboardItemModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.plugin.module.jira.dashboard.ConnectDashboardItemModuleDescriptor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.Sets;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
public class DashboardItemModuleBeanFactory implements ConnectModuleDescriptorFactory<DashboardItemModuleBean, DashboardItemModuleDescriptor>
{
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final ModuleFactory moduleFactory;
    private final PluggableParametersExtractor parametersExtractor;
    private final ModuleContextFilter moduleContextFilter;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;

    @Autowired
    public DashboardItemModuleBeanFactory(final ConditionModuleFragmentFactory conditionModuleFragmentFactory,
            final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            final ModuleFactory moduleFactory,
            final PluggableParametersExtractor parametersExtractor,
            final ModuleContextFilter moduleContextFilter,
            final RemotablePluginAccessorFactory pluginAccessorFactory)
    {
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.moduleFactory = moduleFactory;
        this.parametersExtractor = parametersExtractor;
        this.moduleContextFilter = moduleContextFilter;
        this.pluginAccessorFactory = pluginAccessorFactory;
    }

    @Override
    public DashboardItemModuleDescriptor createModuleDescriptor(final ConnectModuleProviderContext moduleProviderContext,
            final Plugin plugin,
            final DashboardItemModuleBean bean)
    {
        ConnectAddonBean addonBean = moduleProviderContext.getConnectAddonBean();
        VendorBean vendor = addonBean.getVendor();

        DirectoryDefinition directoryDefinition = createDirectoryDefinition(plugin, bean, vendor);

        String moduleKey = bean.getKey(addonBean);

        // register an iframe rendering strategy
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(addonBean.getKey())
                .module(moduleKey)
                .genericBodyTemplate()
                .urlTemplate(bean.getUrl())
                .conditions(bean.getConditions())
                .build();

        ConnectDashboardItemModuleDescriptor moduleDescriptor = new ConnectDashboardItemModuleDescriptor(moduleFactory, directoryDefinition, renderStrategy, moduleContextFilter, parametersExtractor);

        Element dashboardItemModule = new DOMElement("dashboard-item");
        dashboardItemModule.addAttribute("key", moduleKey);
        moduleDescriptor.init(plugin, dashboardItemModule);

        return moduleDescriptor;
    }

    private DirectoryDefinition createDirectoryDefinition(final Plugin plugin,
            final DashboardItemModuleBean moduleBean,
            final VendorBean vendor)
    {
        return new ConnectDashboardItemDirectoryDefintion(moduleBean.getTitle().getRawValue(),
                moduleBean.getTitle().getI18n(),
                author(vendor),
                iconUri(plugin, moduleBean.getIcon()));
    }

    private URI iconUri(final Plugin plugin, final IconBean icon)
    {
        URI iconUri = URI.create(icon.getUrl());
        if (iconUri.isAbsolute())
        {
            return iconUri;
        }
        else
        {
            return pluginAccessorFactory.get(plugin.getKey()).getTargetUrl(iconUri);
        }
    }

    private Author author(VendorBean vendorBean)
    {
        return new ConnectDashboardItemAuthor(vendorBean.getName());
    }

    private static class ConnectDashboardItemDirectoryDefintion implements DirectoryDefinition
    {
        private final String title;
        private final String titleI18nKey;
        private final Author author;
        private final URI thumbnail;

        private ConnectDashboardItemDirectoryDefintion(final String title,
                final String titleI18nKey,
                final Author author,
                final URI thumbnail)
        {
            this.title = title;
            this.titleI18nKey = titleI18nKey;
            this.author = author;
            this.thumbnail = thumbnail;
        }

        @Override
        public String getTitle()
        {
            return title;
        }

        @Override
        public Option<String> getTitleI18nKey()
        {
            return Option.option(titleI18nKey);
        }

        @Override
        public Author getAuthor()
        {
            return author;
        }

        @Override
        public Set<Category> getCategories()
        {
            return Sets.newHashSet(Category.JIRA);
        }

        @Override
        public Option<URI> getThumbnail()
        {
            return Option.some(thumbnail);
        }
    }

    private static class ConnectDashboardItemAuthor implements Author
    {
        private final String fullname;

        private ConnectDashboardItemAuthor(final String fullname)
        {
            this.fullname = fullname;
        }

        @Override
        public String getFullname()
        {
            return fullname;
        }

        @Override
        public Option<String> getEmail()
        {
            return Option.none();
        }
    }


}
