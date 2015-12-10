package com.atlassian.plugin.connect.confluence.theme;

import java.net.URI;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.confluence.plugin.descriptor.ThemeModuleDescriptor;
import com.atlassian.confluence.themes.ExperimentalUnsupportedTheme;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameContextImpl;
import com.atlassian.plugin.connect.api.web.iframe.IFrameParams;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.Maps;

import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class ConfluenceRemoteAddonTheme extends ExperimentalUnsupportedTheme
{
    private final RemotablePluginAccessorFactory accessorFactory;
    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;

    private final EnumMap<LayoutType, String> layoutMap = new EnumMap<>(LayoutType.class);
    private RemotablePluginAccessor remotablePluginAccessor;
    private String addonKey;

    @Autowired
    public ConfluenceRemoteAddonTheme(RemotablePluginAccessorFactory accessorFactory,
                                      UserManager userManager,
                                      ApplicationProperties applicationProperties)
    {
        this.accessorFactory = accessorFactory;
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
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
        remotablePluginAccessor = accessorFactory.get(addonKey);
    }

    public String getRemoteUrl(String layoutName,final Map<String, String> extraParams)
    {
        String url = layoutMap.get(LayoutType.valueOf(layoutName));
        String wrangledUrl = remotablePluginAccessor.signGetUrl(URI.create(url), wrangle(addExtraContexts(extraParams)));
        return wrangledUrl;//iFrameRenderer.render(makeContext(extraParams, url), userManager.getRemoteUser().getUsername());
    }

    private Map<String, String> addExtraContexts(Map<String, String> extraParams)
    {
        HashMap<String, String> combined = Maps.newHashMap(extraParams);
        combined.put("userKey", userManager.getRemoteUserKey().getStringValue());
        combined.put("user_id", userManager.getRemoteUser().getUsername());
        combined.put("xdm_e", applicationProperties.getBaseUrl(UrlMode.ABSOLUTE));
        return combined;
    }

    private IFrameContextImpl makeContext(final Map<String, String> extraParams, String url)
    {
        return new IFrameContextImpl(addonKey, url, "namespace-test", new IFrameParams()
        {
            final HashMap<String, Object> m = Maps.newHashMap(extraParams);

            @Override
            public Map<String, Object> getAsMap() {
                return m;
            }

            @Override
            public void setParam(String key, String value) {
                m.put(key, value);
            }
        });
    }

    private Map<String, String[]> wrangle(Map<String, String> extraParams)
    {
        return Maps.transformValues(extraParams, input -> new String[]{input});
    }
}
