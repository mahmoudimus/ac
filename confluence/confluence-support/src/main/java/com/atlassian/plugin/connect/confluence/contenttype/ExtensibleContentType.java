package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.content.ContentEntityAdapter;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.ContentTypeApiSupport;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.content.custom.BaseCustomContentType;
import com.atlassian.confluence.content.ui.ContentUiSupport;
import com.atlassian.confluence.security.PermissionDelegate;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;


public class ExtensibleContentType extends BaseCustomContentType
{
    private final String contentTypeKey;
    private final ExtensibleContentTypeModuleBean bean;
    private final ContentEntityAdapter contentEntityAdapter;
    private final PermissionDelegate permissionDelegate;
    private final ContentUiSupport contentUiSupport;
    private final CustomContentApiSupportParams customContentApiSupportParams;

    public ExtensibleContentType(
            String contentTypeKey,
            ExtensibleContentTypeModuleBean bean,
            ContentTypeMapper contentTypeMapper,
            ApiSupportProvider apiSupportProvider,
            CustomContentApiSupportParams customContentApiSupportParams)
    {
        super(ContentType.valueOf(contentTypeKey), apiSupportProvider);
        this.contentTypeKey = contentTypeKey;
        this.bean = bean;
        this.permissionDelegate = new ExtensiblePermissionDelegate();
        this.contentEntityAdapter = new ExtensibleContentEntityAdapter(contentTypeMapper);
        this.contentUiSupport = new ExtensibleUISupport();
        this.customContentApiSupportParams = customContentApiSupportParams;
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
        return new ExtensibleContentTypeSupport(contentTypeKey, bean, customContentApiSupportParams);
    }
}
