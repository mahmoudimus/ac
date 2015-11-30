package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.ThemeModuleDescriptor;
import com.atlassian.confluence.themes.ExperimentalUnsupportedTheme;

/**
 *
 */
public class ConfluenceRemoteAddonTheme extends ExperimentalUnsupportedTheme
{
    private String url;
    @Override
    public void init(ThemeModuleDescriptor moduleDescriptor) {
        super.init(moduleDescriptor);
    }
}
