package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ContentEntityAdapter;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.ContentTypeApiSupport;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.content.custom.BaseCustomContentType;
import com.atlassian.confluence.content.ui.ContentUiSupport;
import com.atlassian.confluence.security.PermissionDelegate;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ExtensibleContentType extends BaseCustomContentType
{
    private final ContentEntityAdapter contentEntityAdapter;
    private final PermissionDelegate permissionDelegate;
    private final ContentUiSupport contentUiSupport;
    private final CustomContentApiSupportParams customContentApiSupportParams;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Autowired
    public ExtensibleContentType(final ContentEntityAdapter contentEntityAdapter,
            final PermissionDelegate permissionDelegate,
            final ApiSupportProvider apiSupportProvider,
            final CustomContentApiSupportParams customContentApiSupportParams,
            final RemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        super(ExtensibleContentTypeSupport.contentType, apiSupportProvider);
        this.permissionDelegate = permissionDelegate;
        this.contentEntityAdapter = contentEntityAdapter;
        this.contentUiSupport = new ExtensibleUISupport();
        this.customContentApiSupportParams = customContentApiSupportParams;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
    }

    @Override
    public ContentEntityAdapter getContentAdapter()
    {
        return contentEntityAdapter;
    }

    @Override
    public PermissionDelegate getPermissionDelegate()
    {
        return permissionDelegate;
    }

    @Override
    public ContentUiSupport getContentUiSupport()
    {
        return contentUiSupport;
    }

    @Override
    public ContentTypeApiSupport<CustomContentEntityObject> getApiSupport() {
        return new ExtensibleContentTypeSupport(customContentApiSupportParams);
    }
}
