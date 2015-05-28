package com.atlassian.plugin.connect.core.module.oauth;

import java.security.Principal;

/**
 * Admin system account, only recognized by remote plugin endpoints
 */
public class NonUserAdminPrincipal implements Principal
{
    public static final NonUserAdminPrincipal INSTANCE = new NonUserAdminPrincipal();

    private NonUserAdminPrincipal() {}

    @Override
    public String getName()
    {
        return "__non_user_admin__";
    }
}
