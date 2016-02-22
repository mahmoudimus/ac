package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.annotations.Internal;
import com.atlassian.confluence.themes.ThemeManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Adds a velocity context to connect themes templates. Not intended to be used outside of connect theming.
 */
@ConfluenceComponent
@Internal
public final class ConfluenceRemoteAddonThemeVelocityContext
{
    private final ThemeManager themeManager;

    @Autowired
    public ConfluenceRemoteAddonThemeVelocityContext(@ComponentImport ThemeManager themeManager)
    {
        this.themeManager = themeManager;
    }

    public ThemeManager getThemeManager()
    {
        return themeManager;
    }
}
