package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.ThemeModuleDescriptor;
import com.atlassian.confluence.themes.ExperimentalUnsupportedTheme;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil;
import com.atlassian.plugin.connect.spi.web.context.HashMapModuleContextParameters;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumMap;
import java.util.Map;

/**
 *
 */
public class ConfluenceRemoteAddonTheme extends ExperimentalUnsupportedTheme
{
    private final RemotablePluginAccessorFactory accessorFactory;
    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    private final EnumMap<LayoutType, String> layoutMap = new EnumMap<>(LayoutType.class);
    private RemotablePluginAccessor remotablePluginAccessor;
    private String addonKey;
    private String themeKey;

    @Autowired
    public ConfluenceRemoteAddonTheme(RemotablePluginAccessorFactory accessorFactory,
                                      UserManager userManager,
                                      ApplicationProperties applicationProperties,
                                      IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.accessorFactory = accessorFactory;
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public void init(ThemeModuleDescriptor moduleDescriptor)
    {
        super.init(moduleDescriptor);
        Map<String, String> params = moduleDescriptor.getParams();
        for (LayoutType layoutType : LayoutType.values())
        {
            String overrideTypeName = ConfluenceThemeUtils.getOverrideTypeName(layoutType.name());
            if (params.containsKey(overrideTypeName))
            {
                layoutMap.put(layoutType, params.get(overrideTypeName));
            }
            else
            {
                //TODO: log an error here? something has gone wrong
                throw new IllegalStateException("no " + overrideTypeName + " in the params map");
            }

        }
        addonKey = params.get(ConfluenceThemeModuleDescriptorFactory.ADDON_KEY_PROPERTY_KEY);
        themeKey = params.get(ConfluenceThemeModuleDescriptorFactory.THEME_MODULE_KEY_PROPERTY_KEY);
        remotablePluginAccessor = accessorFactory.get(addonKey);
    }

    @HtmlSafe
    public String getRemoteUrl(LayoutType layoutType, final Map<String, String> extraParams)
    {
        IFrameRenderStrategy iFrameRenderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKey, themeKey, layoutType.name());
        ModuleContextParameters context = new HashMapModuleContextParameters(extraParams);
        context.putAll(extraParams);
        return IFrameRenderStrategyUtil.renderToString(context, iFrameRenderStrategy);
    }

    public String getAddonKey()
    {
        return addonKey;
    }

    public String getThemeKey()
    {
        return themeKey;
    }

    public LayoutConstants getLayoutTypes()
    {
        return new LayoutConstants();
    }

    public static final class LayoutConstants
    {
        public LayoutType getBlog()
        {
            return LayoutType.blog;
        }

        public LayoutType getMain()
        {
            return LayoutType.main;
        }

        public LayoutType getPage()
        {
            return LayoutType.page;
        }
    }
}
