package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.UiOverrideBean;

/**
 *
 */
final class ConfluenceThemeUtils
{
    private ConfluenceThemeUtils() {}

    public static String getThemeKey(ConnectAddonBean addon, ConfluenceThemeModuleBean bean)
    {
        return String.format("%s-remote-theme", bean.getKey(addon));
    }

    public static String getThemeVelocityModuleKey(ConnectAddonBean addon, ConfluenceThemeModuleBean bean)
    {
        return String.format("%s-remote-theme-velocity-context", bean.getKey(addon));
    }

    public static String getThemeVelocityModuleContextKey(ConnectAddonBean addon, ConfluenceThemeModuleBean bean)
    {
        return String.format("%s-remote-theme-velocity-context", bean.getKey(addon));
    }

    public static String getLayoutKey(ConnectAddonBean addon, ConfluenceThemeModuleBean bean, LayoutType type)
    {
        return String.format("%s-remote-theme-layout-%s", bean.getKey(addon), type.name());
    }

    public static String getLayoutName(ConnectAddonBean addon, ConfluenceThemeModuleBean bean, LayoutType type)
    {
        return String.format("Layout for %s type %s", bean.getKey(addon), type.name());
    }

    public static String getOverrideTypeName(UiOverrideBean uiOverrideBean)
    {
        return getOverrideTypeName(uiOverrideBean.getType());
    }

    public static String getOverrideTypeName(final String type)
    {
        return "theme-url-" + LayoutType.valueOf(type).name();
    }
}
