package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.themes.ThemeManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceRemoteAddonThemeVelocityContext
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
