package com.atlassian.plugin.connect.api.web.redirect;

import static com.google.common.base.Preconditions.checkNotNull;

public class RedirectServletPath
{
    public static String forModule(String addOnKey, String moduleKey)
    {
        return "/plugins/servlet/ac-redirect/" + checkNotNull(addOnKey) + "/" + checkNotNull(moduleKey);
    }
}
