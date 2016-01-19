package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;

public class ExtensibleUtils
{
    public static String getContentType(ConnectAddonBean addon, RequiredKeyBean bean)
    {
        return addon.getKey() + ":" + bean.getRawKey();
    }

    public static String getCompleteModuleKey(ConnectAddonBean addon, ExtensibleContentTypeModuleBean bean)
    {
        return addon.getKey() + ":" + bean.getRawKey();
    }
}
