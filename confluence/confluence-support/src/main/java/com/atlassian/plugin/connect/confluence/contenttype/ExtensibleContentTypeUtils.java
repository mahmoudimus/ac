package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;

public class ExtensibleContentTypeUtils {
    public static String getContentType(ConnectAddonBean addon, RequiredKeyBean bean) {
        return addon.getKey() + ":" + bean.getRawKey();
    }

    public static String getExtractorKey(ConnectAddonBean addon, RequiredKeyBean bean) {
        return "extensible-content-type-extractor-" + addon.getKey() + "-" + bean.getRawKey();
    }

    public static String getChangeExtractorKey(ConnectAddonBean addon, RequiredKeyBean bean) {
        return "extensible-content-type-change-extractor-" + addon.getKey() + "-" + bean.getRawKey();
    }
}
