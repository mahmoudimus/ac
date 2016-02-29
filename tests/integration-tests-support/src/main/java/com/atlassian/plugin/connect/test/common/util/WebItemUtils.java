package com.atlassian.plugin.connect.test.common.util;

import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;

public class WebItemUtils {

    public static String linkId(String moduleKey) {
        return String.format("%s:%s", ConnectPluginInfo.getPluginKey(), moduleKey);
    }
}
