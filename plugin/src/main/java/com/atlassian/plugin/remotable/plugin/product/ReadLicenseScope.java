package com.atlassian.plugin.remotable.plugin.product;

import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.spi.permission.AbstractPermission;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

/**
 * Cross-product API Scope for retrieving the information about the plugin license.
 */
public class ReadLicenseScope extends AbstractPermission implements ApiScope
{
    private final RestApiScopeHelper scopeHelper;

    public ReadLicenseScope()
    {
        super(Permissions.READ_LICENSE);
        scopeHelper = new RestApiScopeHelper(asList(
                new RestApiScopeHelper.RestScope("remotable-plugins", asList("1.0", "latest"), "/license", asList("get"))
        ));
    }

    @Override
    public boolean allow(final HttpServletRequest request, final String user)
    {
        return scopeHelper.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return scopeHelper.getApiResourceInfos();
    }
}
