package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;

public class ExtensibleContentTypeUtils {
    public static String getContentType(ConnectAddonBean addon, RequiredKeyBean bean) {
        return addon.getKey() + ":" + bean.getRawKey();
    }

    public static String getSearchBodyPropertyModuleKey(ExtensibleContentTypeModuleBean bean) {
        return "search-body-property-" + bean.getRawKey();
    }
}
