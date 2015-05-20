package com.atlassian.plugin.connect.api.iframe.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectIFrameServletHelper
{
    private ConnectIFrameServletHelper() {}

    public static String iFrameServletPath(String addOnKey, String moduleKey)
    {
        return "/plugins/servlet/ac/" + checkNotNull(addOnKey) + "/" + checkNotNull(moduleKey);
    }
}
