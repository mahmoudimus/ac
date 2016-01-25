package com.atlassian.plugin.connect.api.web.redirect;

import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class RedirectServletPath
{
    private static final String SERVLET_PATH = "/plugins/servlet/ac-redirect/";

    public static String forModule(String addOnKey, String moduleKey)
    {
        checkNotNull(addOnKey);
        checkNotNull(moduleKey);

        // Create complete module key if it was not provided.
        // This url is parsed by connect JS that requires complete module key.
        String completeKey = ModuleKeyUtils.addonAndModuleKey(addOnKey, ModuleKeyUtils.moduleKeyOnly(addOnKey, moduleKey));
        return SERVLET_PATH + addOnKey + "/" + completeKey;
    }
}
