package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.fugue.Option;
import com.atlassian.gadgets.directory.Category;
import com.atlassian.gadgets.plugins.DashboardItemModule.Author;
import com.atlassian.gadgets.plugins.DashboardItemModule.DirectoryDefinition;
import com.atlassian.gadgets.plugins.DashboardItemModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.atlassian.plugin.connect.plugin.capabilities.condition.ConnectConditionElementParserFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.plugin.module.jira.dashboard.ConnectDashboardItemModuleDescriptor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;
import com.google.common.collect.Sets;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
public class DashboardItemModuleDescriptorFactory implements ConnectModuleDescriptorFactory<DashboardItemModuleBean, DashboardItemModuleDescriptor>
{
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ConnectConditionElementParserFactory conditionElementParserFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final ModuleFactory moduleFactory;
    private final PluggableParametersExtractor parametersExtractor;
    private final ModuleContextFilter moduleContextFilter;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;

    @Autowired
    public DashboardItemModuleDescriptorFactory(final ConditionModuleFragmentFactory conditionModuleFragmentFactory,
            final ConnectConditionElementParserFactory conditionElementParserFactory,
            final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            final ModuleFactory moduleFactory,
            final PluggableParametersExtractor parametersExtractor,
            final ModuleContextFilter moduleContextFilter,
            final RemotablePluginAccessorFactory pluginAccessorFactory)
    {
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.conditionElementParserFactory = conditionElementParserFactory;
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

        DirectoryDefinition directoryDefinition = createDirectoryDefinition(addonBean, bean, vendor);

        String moduleKey = bean.getKey(addonBean);

        // register an iframe rendering strategy
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(addonBean.getKey())
                .module(moduleKey)
                .genericBodyTemplate()
                .urlTemplate(bean.getUrl())
                .conditions(bean.getConditions())
                .ensureUniqueNamespace(true)
                .build();

        Condition condition = createCondition(plugin, addonBean, bean);

        ConnectDashboardItemModuleDescriptor moduleDescriptor =
                new ConnectDashboardItemModuleDescriptor(moduleFactory, directoryDefinition, renderStrategy,
                        moduleContextFilter, parametersExtractor, bean.isConfigurable(), bean.getDescription(), condition);

        Element dashboardItemModule = new DOMElement("dashboard-item");
        dashboardItemModule.addAttribute("key", moduleKey);
        moduleDescriptor.init(plugin, dashboardItemModule);

        return moduleDescriptor;
    }

    private Condition createCondition(final Plugin plugin,
            final ConnectAddonBean addonBean,
            final DashboardItemModuleBean bean)
    {
        if (bean.getConditions().isEmpty())
        {
            return new AlwaysDisplayCondition();
        }
        else
        {
            final DOMElement conditionFragment = conditionModuleFragmentFactory.createFragment(addonBean.getKey(), bean.getConditions());

            return conditionElementParserFactory.getConditionElementParser().makeConditions(plugin, conditionFragment, ConditionElementParser.CompositeType.AND);
        }
    }

    private DirectoryDefinition createDirectoryDefinition(final ConnectAddonBean addOnBean,
            final DashboardItemModuleBean moduleBean,
            final VendorBean vendor)
    {
        return new ConnectDashboardItemDirectoryDefintion(moduleBean.getName().getRawValue(),
                moduleBean.getName().getI18n(),
                author(vendor),
                iconUri(addOnBean, moduleBean.getThumbnailUrl()));
    }

    private URI iconUri(final ConnectAddonBean addonBean, final String thumbnailUrl)
    {
        URI iconUri = URI.create(thumbnailUrl);
        if (iconUri.isAbsolute())
        {
            return iconUri;
        }
        else
        {
            return pluginAccessorFactory.get(addonBean.getKey()).getTargetUrl(iconUri);
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
