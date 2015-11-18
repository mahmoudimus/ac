package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.iframe.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.AbstractConnectCoreModuleProvider;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.web.Condition;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

@Component
@ExportAsDevService
public class WebItemModuleProviderImpl extends AbstractConnectCoreModuleProvider<WebItemModuleBean> implements WebItemModuleProvider
{

    private static final WebItemModuleMeta META = new WebItemModuleMeta();

    private static final String DEFAULT_DIALOG_DIMENSION = "100%"; // NB: the client (js) may size the parent of the iframe if the opening is done from JS

    private final WebItemModuleDescriptorFactory webItemFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public WebItemModuleProviderImpl(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            WebItemModuleDescriptorFactory webItemFactory,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            PluginAccessor pluginAccessor)
    {
        super(pluginRetrievalService, schemaValidator);
        this.webItemFactory = webItemFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public ConnectModuleMeta<WebItemModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<WebItemModuleBean> modules, ConnectModuleProviderContext moduleProviderContext)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (WebItemModuleBean bean : modules)
        {
            descriptors.add(beanToDescriptors(moduleProviderContext, pluginRetrievalService.getPlugin(), bean));
            registerIframeRenderStrategy(bean, moduleProviderContext.getConnectAddonBean());
        }
        return descriptors;
    }

    private ModuleDescriptor beanToDescriptors(ConnectModuleProviderContext moduleProviderContext,
            Plugin plugin, WebItemModuleBean bean)
    {
        ModuleDescriptor descriptor;

        final WebItemTargetBean target = bean.getTarget();
        if (bean.isAbsolute() ||
            bean.getContext().equals(AddOnUrlContext.product) ||
            bean.getContext().equals(AddOnUrlContext.addon) && !target.isDialogTarget() && !target.isInlineDialogTarget())
        {
            descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        }
        else
        {
            String localUrl = ConnectIFrameServletPath.forModule(moduleProviderContext.getConnectAddonBean().getKey(), bean.getUrl());

            WebItemModuleBean newBean = newWebItemBean(bean).withUrl(localUrl).build();
            descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, newBean);
        }

        return descriptor;
    }

    private void registerIframeRenderStrategy(WebItemModuleBean webItem, ConnectAddonBean descriptor)
    {
        // Allow a web item which opens in a dialog to be opened programmatically, too
        final WebItemTargetBean target = webItem.getTarget();
        if (target.isDialogTarget() || target.isInlineDialogTarget())
        {
            List<ConditionalBean> iframeConditions = getConditionsForIframe(webItem);
            final IFrameRenderStrategy iFrameRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(descriptor.getKey())
                    .module(webItem.getKey(descriptor))
                    .genericBodyTemplate()
                    .urlTemplate(webItem.getUrl())
                    .title(webItem.getDisplayName())
                    .conditions(iframeConditions)
                    .dimensions(DEFAULT_DIALOG_DIMENSION, DEFAULT_DIALOG_DIMENSION) // the client (js) will size the parent of the iframe
                    .dialog(target.isDialogTarget())
                    .sign(!webItem.getUrl().toLowerCase().startsWith("http")) // don't sign requests to arbitrary URLs (e.g. wikipedia)
                    .build();

            iFrameRenderStrategyRegistry.register(descriptor.getKey(), webItem.getKey(descriptor), iFrameRenderStrategy);
            iFrameRenderStrategyRegistry.register(descriptor.getKey(), webItem.getRawKey(), IFrameRenderStrategyRegistry.RAW_CLASSIFIER, iFrameRenderStrategy);
            iFrameRenderStrategyRegistry.register(descriptor.getKey(), webItem.getRawKey(), iFrameRenderStrategy);
        }
    }

    @VisibleForTesting
    List<ConditionalBean> getConditionsForIframe(WebItemModuleBean webItem)
    {
        List<ConnectConditionClassResolver> conditionClassResolvers = pluginAccessor.getEnabledModulesByClass(ConnectConditionClassResolver.class);
        return filterSingleConditionsRecursively(webItem.getConditions(), new Predicate<SingleConditionBean>()
        {

            @Override
            public boolean test(SingleConditionBean conditionalBean)
            {
                return conditionClassResolvers.stream()
                        .flatMap(new Function<ConnectConditionClassResolver, Stream<ConnectConditionClassResolver.Entry>>()
                        {
                            @Override
                            public Stream<ConnectConditionClassResolver.Entry> apply(ConnectConditionClassResolver resolver)
                            {
                                return resolver.getEntries().stream();
                            }
                        }).map(new Function<ConnectConditionClassResolver.Entry, Optional<Class<? extends Condition>>>()
                        {
                            @Override
                            public Optional<Class<? extends Condition>> apply(ConnectConditionClassResolver.Entry resolverEntry)
                            {
                                return resolverEntry.getConditionClassForNoContext(conditionalBean);
                            }
                        }).filter(new Predicate<Optional<Class<? extends Condition>>>()
                        {
                            @Override
                            public boolean test(Optional<Class<? extends Condition>> optionalConditionClass)
                            {
                                return optionalConditionClass.isPresent();
                            }
                        }).map(new Function<Optional<Class<? extends Condition>>, Class<? extends Condition>>()
                        {
                            @Override
                            public Class<? extends Condition> apply(Optional<Class<? extends Condition>> optionalConditionClass)
                            {
                                return optionalConditionClass.get();
                            }
                        }).findFirst().isPresent();
            }
        });
    }

    private List<ConditionalBean> filterSingleConditionsRecursively(List<ConditionalBean> conditions,
            Predicate<SingleConditionBean> filterPredicate)
    {
        List<ConditionalBean> filteredConditions = new ArrayList<>();
        for (ConditionalBean condition : conditions)
        {
            if (SingleConditionBean.class.isAssignableFrom(condition.getClass()))
            {
                if (filterPredicate.test((SingleConditionBean) condition))
                {
                    filteredConditions.add(condition);
                }
            }
            else
            {
                CompositeConditionBean compositeCondition = (CompositeConditionBean) condition;
                List<ConditionalBean> filteredNestedConditions = filterSingleConditionsRecursively(
                        compositeCondition.getConditions(), filterPredicate);
                if (!filteredNestedConditions.isEmpty())
                {
                    filteredConditions.add(CompositeConditionBean.newCompositeConditionBean(compositeCondition)
                            .withConditions(filteredNestedConditions)
                            .build());
                }
            }
        }
        return filteredConditions;
    }
}
