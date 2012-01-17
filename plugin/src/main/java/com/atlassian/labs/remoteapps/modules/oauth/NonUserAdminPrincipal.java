package com.atlassian.labs.remoteapps.modules.oauth;

import java.security.Principal;

/**
 * Admin system account, only recognized by remote app endpoints
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
