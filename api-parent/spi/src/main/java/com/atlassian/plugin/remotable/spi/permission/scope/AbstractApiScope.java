package com.atlassian.plugin.remotable.spi.permission.scope;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.permission.AbstractPermission;
import com.atlassian.plugin.remotable.spi.permission.PermissionInfo;

import javax.servlet.http.HttpServletRequest;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractApiScope extends AbstractPermission implements ApiScope
{
    private final RestApiScopeHelper restApiScopeHelper;

    public AbstractApiScope(String key, Set<InstallationMode> installationModes, RestApiScopeHelper restApiScopeHelper)
    {
        super(key, installationModes);
        this.restApiScopeHelper = checkNotNull(restApiScopeHelper);
    }

    public AbstractApiScope(String key, Set<InstallationMode> installationModes, RestApiScopeHelper restApiScopeHelper, PermissionInfo permissionInfo)
    {
        super(key, installationModes, permissionInfo);
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
