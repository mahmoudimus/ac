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
import com.atlassian.plugin.connect.spi.module.provider.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleValidationException;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableList;

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

    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final PageConditionsFactory pageConditionsFactory;

    public AbstractConnectPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                             IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                             PageConditionsFactory pageConditionsFactory)
    {
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
        this.pageConditionsFactory = pageConditionsFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<ConnectPageModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        for (ConnectPageModuleBean bean : beans)
        {
            // register a render strategy for our iframe page
            IFrameRenderStrategy pageRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(connectAddonBean.getKey())
                    .module(bean.getKey(connectAddonBean))
                    .pageTemplate()
                    .urlTemplate(bean.getUrl())
                    .decorator(getDecorator())
                    .conditions(bean.getConditions())
                    .conditionClasses(getConditionClasses())
                    .title(bean.getDisplayName())
                    .resizeToParent(true)
                    .build();
            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), pageRenderStrategy);

            // and an additional strategy for raw content, in case the user wants to use it as a dialog target
            IFrameRenderStrategy rawRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(connectAddonBean.getKey())
                    .module(bean.getKey(connectAddonBean))
                    .genericBodyTemplate()
                    .urlTemplate(bean.getUrl())
                    .conditions(bean.getConditions())
                    .conditionClasses(getConditionClasses())
                    .dimensions("100%", "100%") // the client (js) will size the parent of the iframe
                    .build();
            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), RAW_CLASSIFIER, rawRenderStrategy);

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

                builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin,
                        webItemBean, getConditionClasses()));
            }
        }

        return builder.build();
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

    @Override
    public List<ConnectPageModuleBean> validate(String rawModules, Class<ConnectPageModuleBean> type, Plugin plugin, ShallowConnectAddonBean bean) throws ConnectModuleValidationException
    {
        List<ConnectPageModuleBean> pageBeans = super.validate(rawModules, type, plugin, bean);
        validateConditions(pageBeans);
        return pageBeans;
    }

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
