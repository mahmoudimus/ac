package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.page;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet.RAW_CLASSIFIER;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Base class for ConnectModuleProviders of Connect Pages. Note that there is actually no P2 module descriptor. Instead
 * it is modelled as a web-item plus a servlet
 */
public abstract class AbstractConnectPageModuleProvider implements ConnectModuleProvider<ConnectPageModuleBean>
{
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;

    public AbstractConnectPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory)
    {
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName,
                                                 List<ConnectPageModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectPageModuleBean bean : beans)
        {
            // register a render strategy for our iframe page
            IFrameRenderStrategy pageRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(addon.getKey())
                    .module(bean.getKey())
                    .pageTemplate()
                    .urlTemplate(bean.getUrl())
                    .decorator(getDecorator())
                    .conditions(bean.getConditions())
                    .conditionClasses(getConditionClasses())
                    .title(bean.getDisplayName())
                    .resizeToParent(true)
                    .build();
            iFrameRenderStrategyRegistry.register(addon.getKey(), bean.getKey(), pageRenderStrategy);

            // and an additional strategy for raw content, in case the user wants to use it as a dialog target
            IFrameRenderStrategy rawRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(addon.getKey())
                    .module(bean.getKey())
                    .genericBodyTemplate()
                    .urlTemplate(bean.getUrl())
                    .conditions(bean.getConditions())
                    .conditionClasses(getConditionClasses())
                    .dimensions("100%", "100%") // the client (js) will size the parent of the iframe
                    .build();
            iFrameRenderStrategyRegistry.register(addon.getKey(), bean.getKey(), RAW_CLASSIFIER, rawRenderStrategy);

            if (hasWebItem())
            {
                // create a web item targeting the iframe page
                Integer weight = bean.getWeight() == null ? getDefaultWeight() : bean.getWeight();
                String location = isNullOrEmpty(bean.getLocation()) ? getDefaultSection() : bean.getLocation();

                WebItemModuleBean webItemBean = newWebItemBean()
                        .withName(bean.getName())
                        .withKey(bean.getKey())
                        .withContext(page)
                        .withUrl(ConnectIFrameServlet.iFrameServletPath(addon.getKey(), bean.getKey()))
                        .withLocation(location)
                        .withWeight(weight)
                        .withIcon(bean.getIcon())
                        .withConditions(bean.getConditions())
                        .build();

                builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(addon, theConnectPlugin, webItemBean, getConditionClasses()));
            }
        }

        return builder.build();
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

}
