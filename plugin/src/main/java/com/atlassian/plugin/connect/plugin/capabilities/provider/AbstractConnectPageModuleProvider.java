package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;
import java.util.Map;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet;
import com.atlassian.plugin.web.Condition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.product;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
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
    public List<ModuleDescriptor> provideModules(Plugin plugin, String jsonFieldName,
                                                 List<ConnectPageModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectPageModuleBean bean : beans)
        {
            // register a render strategy for our iframe page
            Map<String, Object> additionalRenderContext = Maps.newHashMap();
            augmentRenderContext(additionalRenderContext);
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(plugin.getKey())
                    .module(bean.getKey())
                    .genericPageTemplate()
                    .urlTemplate(bean.getUrl())
                    .decorator(getDecorator())
                    .condition(getCondition())
                    .title(bean.getDisplayName())
                    .additionalRenderContext(additionalRenderContext)
                    .build();

            iFrameRenderStrategyRegistry.register(plugin.getKey(), bean.getKey(), renderStrategy);

            if (hasWebItem())
            {
                // create a web item targeting the iframe page
                Integer weight = bean.getWeight() == null ? getDefaultWeight() : bean.getWeight();
                String location = isNullOrEmpty(bean.getLocation()) ? getDefaultSection() : bean.getLocation();

                WebItemModuleBean webItemBean = newWebItemBean()
                        .withName(bean.getName())
                        .withKey(bean.getKey())
                        .withContext(product)
                        .withUrl(ConnectIFrameServlet.iFrameServletPath(plugin.getKey(), bean.getKey()))
                        .withLocation(location)
                        .withWeight(weight)
                        .withIcon(bean.getIcon())
                        .withConditions(bean.getConditions())
                        .build();

                builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, webItemBean));
            }
        }

        return builder.build();
    }

    protected Condition getCondition()
    {
        return null;
    }

    protected boolean hasWebItem()
    {
        return true;
    }

    protected void augmentRenderContext(Map<String, Object> additionalRenderContext)
    {
        // no additional context by default
    }

    protected abstract String getDecorator();

    protected abstract String getDefaultSection();

    protected abstract int getDefaultWeight();

}
