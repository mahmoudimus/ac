package com.atlassian.plugin.connect.plugin.oldscopes.jira;

import com.atlassian.plugin.connect.api.jira.JiraPermissions;
import com.atlassian.plugin.connect.spi.permission.AbstractPermission;
import com.atlassian.plugin.connect.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.permission.scope.ApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.DownloadScopeHelper;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * Scope which allows to access issue attachments.
 */
public class JiraDownloadAttachmentScope extends AbstractPermission implements ApiScope
{
    private final DownloadScopeHelper delegate;

    public JiraDownloadAttachmentScope()
    {
        super(JiraPermissions.DOWNLOAD_ATTACHMENTS);
        this.delegate = new DownloadScopeHelper("/secure/attachment");
    }

    @Override
    public boolean allow(final HttpServletRequest request, @Nullable final String user)
    {
        return delegate.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return delegate.getApiResourceInfos();
    }
}
