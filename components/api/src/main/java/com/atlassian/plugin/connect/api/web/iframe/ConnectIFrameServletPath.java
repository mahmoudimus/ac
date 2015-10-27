package com.atlassian.plugin.connect.api.web.iframe;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectIFrameServletPath
{
    private ConnectIFrameServletPath() {}

    public static String forModule(String addOnKey, String moduleKey)
    {
        return "/plugins/servlet/ac/" + checkNotNull(addOnKey) + "/" + checkNotNull(moduleKey);
    }
}
