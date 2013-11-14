package com.atlassian.plugin.connect.plugin.module.permission;

import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.product.WebSudoService;
import com.atlassian.sal.api.user.UserManager;

/**
 * A filter to restrict incoming requests unless they have been authorized via api scopes.
 * Handles requests by looking for the add-on key as a request attribute.
 */
public class ApiScopingFilter extends ApiScopingFilterBase
{
    protected ApiScopingFilter(PermissionManager permissionManager, UserManager userManager, WebSudoService webSudoService, String ourConsumerKey)
    {
        super(permissionManager, userManager, webSudoService, ourConsumerKey);
    }
}
