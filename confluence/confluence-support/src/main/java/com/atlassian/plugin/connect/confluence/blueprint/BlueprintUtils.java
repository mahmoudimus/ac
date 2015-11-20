package com.atlassian.plugin.connect.confluence.blueprint;

import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;

public class BlueprintUtils {
    private BlueprintUtils() {}

    public static String getWebItemKey(ConnectAddonBean addon, RequiredKeyBean bean) {
        return bean.getKey(addon)+"-web-item";
    }

    public static String getBlueprintKey(ConnectAddonBean addon, BlueprintModuleBean bean) {
        return bean.getKey(addon)+"-blueprint";
    }

    public static String getContentTemplateKey(ConnectAddonBean addon, BlueprintModuleBean bean) {
        return bean.getKey(addon)+"-content-template";
    }

    public static String getContextUrl(ConnectAddonBean addon, BlueprintModuleBean bean) {
        return addon.getBaseUrl() + bean.getBlueprintTemplate().getContext();
    }
}
