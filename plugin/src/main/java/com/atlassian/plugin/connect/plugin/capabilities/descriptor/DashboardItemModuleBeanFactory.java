package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.fugue.Option;
import com.atlassian.gadgets.directory.Category;
import com.atlassian.gadgets.plugins.DashboardItemModule;
import com.atlassian.gadgets.plugins.DashboardItemModule.Author;
import com.atlassian.gadgets.plugins.DashboardItemModule.DirectoryDefinition;
import com.atlassian.gadgets.plugins.DashboardItemModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.module.jira.dashboard.ConnectDashboardItemModuleDescriptor;
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
    private final ConnectContainerUtil containerUtil;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final ModuleFactory moduleFactory;

    @Autowired
    public DashboardItemModuleBeanFactory(final ConnectContainerUtil containerUtil,
            final ConditionModuleFragmentFactory conditionModuleFragmentFactory,
            final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            final ModuleFactory moduleFactory)
    {
        this.containerUtil = containerUtil;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.moduleFactory = moduleFactory;
    }

    @Override
    public DashboardItemModuleDescriptor createModuleDescriptor(final ConnectModuleProviderContext moduleProviderContext,
            final Plugin plugin,
            final DashboardItemModuleBean bean)
    {
        ConnectAddonBean addonBean = moduleProviderContext.getConnectAddonBean();
        VendorBean vendor = addonBean.getVendor();

        DirectoryDefinition directoryDefinition = createDirectoryDefinition(bean, vendor);

        ConnectDashboardItemModuleDescriptor moduleDescriptor = new ConnectDashboardItemModuleDescriptor(moduleFactory, directoryDefinition);
        Element dashboardItemModule = new DOMElement("dashboard-item");
        moduleDescriptor.init(plugin, dashboardItemModule);

        return moduleDescriptor;
    }

    private DirectoryDefinition createDirectoryDefinition(final DashboardItemModuleBean moduleBean, final VendorBean vendor)
    {
        return new ConnectDashboardItemDirectoryDefintion(moduleBean.getTitle().getRawValue(),
                moduleBean.getTitle().getI18n(),
                author(vendor),
                Sets.<Category>newHashSet(),
                Option.option(URI.create(moduleBean.getIcon().getUrl())));
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
        private final Set<Category> categories;
        private final Option<URI> thumbnail;

        private ConnectDashboardItemDirectoryDefintion(final String title,
                final String titleI18nKey,
                final Author author,
                final Set<Category> categories,
                final Option<URI> thumbnail)
        {
            this.title = title;
            this.titleI18nKey = titleI18nKey;
            this.author = author;
            this.categories = categories;
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
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Option<URI> getThumbnail()
        {
            throw new UnsupportedOperationException("Not implemented");
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
