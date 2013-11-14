package com.atlassian.plugin.connect.plugin.module.permission;

import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.product.WebSudoService;
import com.atlassian.sal.api.user.UserManager;

/**
 * A filter to restrict incoming requests unless they have been authorized via api scopes.  Only handles 2LO-authenticated
 * requests by looking for the client key as a request attribute or a header.
 * @deprecated Uses legacy scopes implementation. Superseded by {@link ApiScopingFilter}.
 */
@Deprecated
public class LegacyApiScopingFilter extends ApiScopingFilterBase
{
    public LegacyApiScopingFilter(PermissionManager permissionManager, UserManager userManager, WebSudoService webSudoService, String ourConsumerKey)
    {
        super(permissionManager, userManager, webSudoService, ourConsumerKey);
    }
}
