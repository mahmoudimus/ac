package com.atlassian.plugin.connect.spi.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.iframe.servlet.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.TestConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.page;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Base class for ConnectModuleProviders of Connect Pages. Note that there is actually no P2 module descriptor. Instead
 * it is modelled as a web-item plus a servlet
 */
public abstract class AbstractConnectPageTestModuleProvider implements TestConnectModuleProvider
{
    private static final String RAW_CLASSIFIER = "raw";
    private static final Class BEAN_CLASS = ConnectPageModuleBean.class;

    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;

    public AbstractConnectPageTestModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                             IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory)
    {
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<JsonObject> modules)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        for (JsonObject module : modules)
        {
            ConnectPageModuleBean bean = new Gson().fromJson(module, ConnectPageModuleBean.class);
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

    @Override
    public Class getBeanClass()
    {
        return BEAN_CLASS;
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

}
