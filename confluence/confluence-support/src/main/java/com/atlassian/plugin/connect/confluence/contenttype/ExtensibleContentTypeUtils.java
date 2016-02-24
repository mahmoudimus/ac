package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;

public class ExtensibleContentTypeUtils {
    public static String getContentType(ConnectAddonBean addon, RequiredKeyBean bean) {
        return bean.getRawKey();
    }

    public static String getCompleteContentType(ConnectAddonBean addon, RequiredKeyBean bean) {
        return addon.getKey() + ":" + bean.getRawKey();
    }

    public static String getExtractorKey(ConnectAddonBean addon, RequiredKeyBean bean) {
        return "extensibleContentTypeExtractor-" + addon.getKey() + "-" + bean.getRawKey();
    }

    public static String getChangeExtractorKey(ConnectAddonBean addon, RequiredKeyBean bean) {
        return "extensibleContentTypeChangeExtractor-" + addon.getKey() + "-" + bean.getRawKey();
    }
}
