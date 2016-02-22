package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.annotations.Internal;
import com.atlassian.confluence.plugin.descriptor.ThemeModuleDescriptor;
import com.atlassian.confluence.themes.ExperimentalUnsupportedTheme;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil;
import com.atlassian.plugin.connect.spi.web.context.HashMapModuleContextParameters;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

/**
 * A theme implementation which delegates the actual theming to an iframe from a connect addon.
 * The creation of the theme module in connect must also register the iframe rendering strategy for all possible
 * navigation targets ({@link NavigationTargetName}.
 *
 * Do not rename this class, since it's name is used in the theme module descriptor xml, which is saved in the database.
 */
@Internal
public final class ConfluenceRemoteAddonTheme extends ExperimentalUnsupportedTheme
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    private String addonKey;
    private String themeKey;

    @Autowired
    public ConfluenceRemoteAddonTheme(IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public void init(ThemeModuleDescriptor moduleDescriptor)
    {
        super.init(moduleDescriptor);
        Map<String, String> params = moduleDescriptor.getParams();
        addonKey = params.get(ConfluenceThemeModuleDescriptorFactory.ADDON_KEY_PROPERTY_KEY);
        themeKey = params.get(ConfluenceThemeModuleDescriptorFactory.THEME_MODULE_KEY_PROPERTY_KEY);
    }

    @HtmlSafe
    public String getRemoteThemeIframe(NavigationTargetOverrideInfo navigationTargetOverrideInfo, final Map<String, String> extraParams)
    {
        IFrameRenderStrategy iFrameRenderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKey, themeKey, navigationTargetOverrideInfo.name());
        ModuleContextParameters context = makeThemeModuleParametersMap(extraParams);
        return IFrameRenderStrategyUtil.renderToString(context, iFrameRenderStrategy);
    }

    private ModuleContextParameters makeThemeModuleParametersMap(Map<String, String> extraParams)
    {
        ModuleContextParameters context = new HashMapModuleContextParameters(Collections.emptyMap());
        context.putAll(extraParams);
        return context;
    }

    public String getAddonKey()
    {
        return addonKey;
    }

    public Constants getLayoutTypes()
    {
        return new Constants();
    }

    /**
     * A class to hold some constants, for use in velocity templates to ensure type safety and ease of refactor.
     * Not intended to be used by other plugins.
     */
    @Internal
    public static final class Constants
    {
        public NavigationTargetOverrideInfo getBlog()
        {
            return NavigationTargetOverrideInfo.blogpost;
        }

        public NavigationTargetOverrideInfo getMain()
        {
            return NavigationTargetOverrideInfo.dashboard;
        }

        public NavigationTargetOverrideInfo getPage()
        {
            return NavigationTargetOverrideInfo.page;
        }

        public NavigationTargetOverrideInfo getSpace()
        {
            return NavigationTargetOverrideInfo.space;
        }
    }
}
