package com.atlassian.plugin.connect.api.web.redirect;

import static com.google.common.base.Preconditions.checkNotNull;

public class RedirectServletPath
{
    private static final String SERVLET_PATH = "/plugins/servlet/ac-redirect/";

    public static String forModule(String addOnKey, String moduleKey)
    {
        return SERVLET_PATH + checkNotNull(addOnKey) + "/" + checkNotNull(moduleKey);
    }
}
