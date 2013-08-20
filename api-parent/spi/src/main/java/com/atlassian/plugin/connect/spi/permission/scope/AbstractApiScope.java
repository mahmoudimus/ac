package com.atlassian.plugin.connect.spi.permission.scope;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.spi.permission.AbstractPermission;
import com.atlassian.plugin.connect.spi.permission.PermissionInfo;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractApiScope extends AbstractPermission implements ApiScope
{
    private final RestApiScopeHelper restApiScopeHelper;

    public AbstractApiScope(String key, RestApiScopeHelper restApiScopeHelper)
    {
        super(key);
        this.restApiScopeHelper = checkNotNull(restApiScopeHelper);
    }

    public AbstractApiScope(String key, RestApiScopeHelper restApiScopeHelper, PermissionInfo permissionInfo)
    {
        super(key, permissionInfo);
        this.restApiScopeHelper = checkNotNull(restApiScopeHelper);
    }

    @Override
    public final boolean allow(HttpServletRequest request, String user)
    {
        return restApiScopeHelper.allow(request, user);
    }

    @Override
    public final Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return restApiScopeHelper.getApiResourceInfos();
    }
}
