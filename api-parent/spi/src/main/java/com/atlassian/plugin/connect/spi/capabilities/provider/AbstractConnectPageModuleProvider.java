package com.atlassian.plugin.connect.spi.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.iframe.servlet.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ConditionUtils;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.web.Condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.page;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.util.ConditionUtils.isRemoteCondition;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Base class for ConnectModuleProviders of Connect Pages. Note that there is actually no P2 module descriptor. Instead
 * it is modelled as a web-item plus a servlet
 */
public abstract class AbstractConnectPageModuleProvider extends AbstractConnectModuleProvider<ConnectPageModuleBean>
{
    private static final String RAW_CLASSIFIER = "raw";

    private final PluginRetrievalService pluginRetrievalService;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final PageConditionsFactory pageConditionsFactory;

    public AbstractConnectPageModuleProvider(PluginRetrievalService pluginRetrievalService,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            PageConditionsFactory pageConditionsFactory)
    {
        this.pluginRetrievalService = pluginRetrievalService;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
        this.pageConditionsFactory = pageConditionsFactory;
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry,
            ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        List<ConnectPageModuleBean> pageBeans = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        validateConditions(pageBeans);
        return pageBeans;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConnectPageModuleBean> modules, ConnectModuleProviderContext moduleProviderContext)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        for (ConnectPageModuleBean bean : modules)
        {
            if (hasWebItem())
            {
                // create a web item targeting the iframe page
                Integer weight = bean.getWeight() == null ? getDefaultWeight() : bean.getWeight();
                String location = isNullOrEmpty(bean.getLocation()) ? getDefaultSection() : bean.getLocation();

                WebItemModuleBean webItemBean = newWebItemBean()
                        .withName(bean.getName())
                        .withKey(bean.getRawKey())
                        .withContext(page)
                        .withUrl(ConnectIFrameServletPath.forModule(connectAddonBean.getKey(), bean.getRawKey()))
                        .withLocation(location)
                        .withWeight(weight)
                        .withIcon(bean.getIcon())
                        .withConditions(bean.getConditions())
                        .setNeedsEscaping(needsEscaping())
                        .build();

                descriptors.add(webItemModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext,
                        pluginRetrievalService.getPlugin(), webItemBean, getConditionClasses()));
            }

            registerIframeRenderStrategy(bean, connectAddonBean);
        }

        return descriptors;
    }

    protected void registerIframeRenderStrategy(ConnectPageModuleBean page, ConnectAddonBean connectAddonBean)
    {
        // register a render strategy for our iframe page
        IFrameRenderStrategy pageRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(connectAddonBean.getKey())
                .module(page.getKey(connectAddonBean))
                .pageTemplate()
                .urlTemplate(page.getUrl())
                .decorator(getDecorator())
                .conditions(page.getConditions())
                .conditionClasses(getConditionClasses())
                .title(page.getDisplayName())
                .resizeToParent(true)
                .build();
        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), page.getRawKey(), pageRenderStrategy);

        // and an additional strategy for raw content, in case the user wants to use it as a dialog target
        IFrameRenderStrategy rawRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(connectAddonBean.getKey())
                .module(page.getKey(connectAddonBean))
                .genericBodyTemplate()
                .urlTemplate(page.getUrl())
                .conditions(page.getConditions())
                .conditionClasses(getConditionClasses())
                .dimensions("100%", "100%") // the client (js) will size the parent of the iframe
                .build();
        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), page.getRawKey(), RAW_CLASSIFIER, rawRenderStrategy);
    }

    protected boolean needsEscaping()
    {
        return true;
    }

    protected Iterable<Class<? extends Condition>> getConditionClasses()
    {
        return Collections.emptyList();
    }

    protected boolean hasWebItem()
    {
        return true;
    }

    protected abstract String getDecorator();

    protected abstract String getDefaultSection();

    protected abstract int getDefaultWeight();

    protected void validateConditions(List<ConnectPageModuleBean> pageBeans) throws ConnectModuleValidationException
    {
        for (ConnectPageModuleBean page : pageBeans)
        {
            for (SingleConditionBean condition : ConditionUtils.getSingleConditionsRecursively(page.getConditions()))
            {
                assertValidPageCondition(page, condition.getCondition());
            }
        }
    }

    private void assertValidPageCondition(ConnectPageModuleBean page, String conditionString) throws ConnectModuleValidationException
    {
        if (!pageConditionsFactory.getConditionNames().contains(conditionString) && !isRemoteCondition(conditionString))
        {
            String exceptionMessage = String.format("The add-on (%s) includes a Page Module with an unsupported condition (%s)", page.getRawKey(), conditionString);
            throw new ConnectModuleValidationException(getMeta().getDescriptorKey(), exceptionMessage);
        }
    }
}
