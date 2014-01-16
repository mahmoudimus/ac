package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.PageToWebItemAndServletConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for ConnectModuleProviders of Connect Pages. Note that there is actually no P2 module descriptor. Instead
 * it is modelled as a web-item plus a servlet
 */
public abstract class AbstractConnectPageModuleProvider implements ConnectModuleProvider<ConnectPageModuleBean>
{
    private final IFrameParams iFrameParams;
    private final String defaultSection;
    private final int defaultWeight;

    public static class ConnectPageIFrameParams extends IFrameParamsImpl
    {
        /**
         * Used in UI to change sizing etc
         */
        public void setIsGeneralPage()
        {
            setParam("general", "1");
        }

        public static IFrameParams withGeneralPage()
        {
            ConnectPageIFrameParams params = new ConnectPageIFrameParams();
            params.setIsGeneralPage();
            return params;
        }
    }

    private final IFrameRenderStrategyFactory iFrameRenderStrategyFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final String decorator;
    private final String templateSuffix;
    private final Map<String, String> metaTagContents;
    private final Condition condition;

    public AbstractConnectPageModuleProvider(IFrameRenderStrategyFactory iFrameRenderStrategyFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            String decorator, String defaultSection, int defaultWeight,
            String templateSuffix, Map<String, String> metaTagContents,
            Condition condition, @Nullable IFrameParams iFrameParams)
    {
        this.iFrameRenderStrategyFactory = iFrameRenderStrategyFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.defaultSection = defaultSection;
        this.defaultWeight = defaultWeight;
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
        this.decorator = decorator;
        this.templateSuffix = templateSuffix;
        this.metaTagContents = metaTagContents;
        this.condition = condition;
        this.iFrameParams = iFrameParams;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName,
            List<ConnectPageModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectPageModuleBean bean : beans)
        {
            PageToWebItemAndServletConverter converter = new PageToWebItemAndServletConverter(bean, plugin.getKey(),
                    defaultWeight, defaultSection, decorator, templateSuffix, metaTagContents, condition, iFrameParams);
            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, converter.getWebItemBean()));

            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyFactory.page(plugin.getKey(), bean.getKey(),
                    bean.getUrl(), "velocity/iframe-page" + templateSuffix + ".vm", decorator, bean.getDisplayName());
            iFrameRenderStrategyRegistry.register(plugin.getKey(), bean.getKey(), renderStrategy);
        }

        return builder.build();
    }
}
