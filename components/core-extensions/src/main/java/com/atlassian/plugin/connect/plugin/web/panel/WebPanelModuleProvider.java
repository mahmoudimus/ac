package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.WebFragmentLocationBlacklist;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.web.redirect.RedirectData;
import com.atlassian.plugin.connect.api.web.redirect.RedirectDataBuilderFactory;
import com.atlassian.plugin.connect.api.web.redirect.RedirectRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleMeta;
import com.atlassian.plugin.connect.plugin.AbstractConnectCoreModuleProvider;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.atlassian.plugin.connect.api.web.redirect.RedirectData.AccessDeniedTemplateType.IFRAME;

public class WebPanelModuleProvider extends AbstractConnectCoreModuleProvider<WebPanelModuleBean>
{
    private static final WebPanelModuleMeta META = new WebPanelModuleMeta();

    private final WebPanelConnectModuleDescriptorFactory webPanelFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final WebFragmentLocationBlacklist webFragmentLocationBlacklist;
    private final ConditionLoadingValidator conditionLoadingValidator;
    private final RedirectRegistry redirectRegistry;
    private final RedirectDataBuilderFactory redirectDataBuilderFactory;
    private final MovableWebSectionSearcher movableWebSectionSearcher;

    public WebPanelModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            WebPanelConnectModuleDescriptorFactory webPanelFactory,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebFragmentLocationBlacklist webFragmentLocationBlacklist,
            ConditionLoadingValidator conditionLoadingValidator,
            RedirectRegistry redirectRegistry,
            RedirectDataBuilderFactory redirectDataBuilderFactory,
            MovableWebSectionSearcher movableWebSectionSearcher)
    {
        super(pluginRetrievalService, schemaValidator);
        this.webPanelFactory = webPanelFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webFragmentLocationBlacklist = webFragmentLocationBlacklist;
        this.conditionLoadingValidator = conditionLoadingValidator;
        this.redirectRegistry = redirectRegistry;
        this.redirectDataBuilderFactory = redirectDataBuilderFactory;
        this.movableWebSectionSearcher = movableWebSectionSearcher;
    }

    @Override
    public ConnectModuleMeta<WebPanelModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<WebPanelModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        List<WebPanelModuleBean> webPanels = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), webPanels);
        assertLocationNotBlacklisted(descriptor, webPanels);
        return webPanels;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<WebPanelModuleBean> modules, ConnectAddonBean addon)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (WebPanelModuleBean webPanel : modules)
        {
            registerIframeRenderStrategy(webPanel, addon);
            descriptors.add(webPanelFactory.createModuleDescriptor(webPanel, addon, pluginRetrievalService.getPlugin()));
        }
        return descriptors;
    }

    private void assertLocationNotBlacklisted(ShallowConnectAddonBean descriptor, List<WebPanelModuleBean> webPanelModuleBeans) throws ConnectModuleValidationException
    {
        List<String> blacklistedLocationsUsed = webPanelModuleBeans.stream()
                .filter(new Predicate<WebPanelModuleBean>()
                {
                    @Override
                    public boolean test(WebPanelModuleBean webPanel)
                    {
                        return webFragmentLocationBlacklist.getBlacklistedWebPanelLocations().contains(webPanel.getLocation());
                    }
                })
                .map(new Function<WebPanelModuleBean, String>()
                {
                    @Override
                    public String apply(WebPanelModuleBean webPanelModuleBean)
                    {
                        return webPanelModuleBean.getLocation();
                    }
                })
                .collect(Collectors.toList());

        if (blacklistedLocationsUsed.size() > 0)
        {
            String exceptionMsg = String.format("Installation failed. The add-on includes a web fragment with an unsupported location (%s).", blacklistedLocationsUsed);
            throw new ConnectModuleValidationException(descriptor, getMeta(), exceptionMsg, "connect.install.error.invalid.location", blacklistedLocationsUsed.toArray(new String[blacklistedLocationsUsed.size()]));
        }
    }

    private void registerIframeRenderStrategy(WebPanelModuleBean webPanel, ConnectAddonBean descriptor)
    {
        boolean webPanelNeedsRedirection = movableWebSectionSearcher.isWebPanelInMovableWebSection(webPanel, descriptor);

        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addon(descriptor.getKey())
                .module(webPanel.getKey(descriptor))
                .genericBodyTemplate()
                .urlTemplate(webPanel.getUrl())
                .title(webPanel.getDisplayName())
                .redirect(webPanelNeedsRedirection)
                .dimensions(webPanel.getLayout().getWidth(), webPanel.getLayout().getHeight())
                .build();
        iFrameRenderStrategyRegistry.register(descriptor.getKey(), webPanel.getRawKey(), renderStrategy);

        if (webPanelNeedsRedirection)
        {
            RedirectData redirectData = redirectDataBuilderFactory.builder()
                    .addOn(descriptor.getKey())
                    .urlTemplate(webPanel.getUrl())
                    .accessDeniedTemplateType(IFRAME)
                    .title(webPanel.getDisplayName())
                    .conditions(webPanel.getConditions())
                    .build();

            redirectRegistry.register(descriptor.getKey(), webPanel.getRawKey(), redirectData);
        }
    }
}
