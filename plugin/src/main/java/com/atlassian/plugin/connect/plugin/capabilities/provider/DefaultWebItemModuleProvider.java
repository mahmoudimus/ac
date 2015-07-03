package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.iframe.servlet.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet.RAW_CLASSIFIER;

@Component
@ExportAsDevService
public class DefaultWebItemModuleProvider implements WebItemModuleProvider
{
    public static final String DESCRIPTOR_KEY = "webItems";
    public static final Class BEAN_CLASS = WebItemModuleBean.class;
    private static final String DEFAULT_DIALOG_DIMENSION = "100%"; // NB: the client (js) may size the parent of the iframe if the opening is done from JS

    private final WebItemModuleDescriptorFactory webItemFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public DefaultWebItemModuleProvider(WebItemModuleDescriptorFactory webItemFactory,
                                        IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                        IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.webItemFactory = webItemFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<JsonObject> modules)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (JsonObject module : modules)
        {
            WebItemModuleBean bean = new Gson().fromJson(module, WebItemModuleBean.class);
            descriptors.addAll(beanToDescriptors(moduleProviderContext, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(ConnectModuleProviderContext moduleProviderContext,
                                                                     Plugin theConnectPlugin, WebItemModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        final WebItemTargetBean target = bean.getTarget();

        if (bean.isAbsolute() ||
            bean.getContext().equals(AddOnUrlContext.product) ||
            bean.getContext().equals(AddOnUrlContext.addon) && !target.isDialogTarget() && !target.isInlineDialogTarget())
        {
            descriptors.add(webItemFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean));
        }
        else
        {
            String localUrl = ConnectIFrameServletPath.forModule(connectAddonBean.getKey(), bean.getUrl());

            WebItemModuleBean newBean = newWebItemBean(bean).withUrl(localUrl).build();
            descriptors.add(webItemFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, newBean));
        }
        // Allow a web item which opens in a dialog to be opened programmatically, too
        if (target.isDialogTarget() || target.isInlineDialogTarget())
        {
            final IFrameRenderStrategy iFrameRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(connectAddonBean.getKey())
                    .module(bean.getKey(connectAddonBean))
                    .genericBodyTemplate()
                    .urlTemplate(bean.getUrl())
                    .title(bean.getDisplayName())
                    .conditions(bean.getConditions())
                    .dimensions(DEFAULT_DIALOG_DIMENSION, DEFAULT_DIALOG_DIMENSION) // the client (js) will size the parent of the iframe
                    .dialog(target.isDialogTarget())
                    .sign(!bean.getUrl().toLowerCase().startsWith("http")) // don't sign requests to arbitrary URLs (e.g. wikipedia)
                    .build();

            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getKey(connectAddonBean), iFrameRenderStrategy);
            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), RAW_CLASSIFIER, iFrameRenderStrategy);
            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), iFrameRenderStrategy);
        }

        return descriptors;
    }

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }

    @Override
    public Class getBeanClass()
    {
        return BEAN_CLASS;
    }

}
