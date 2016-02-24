package com.atlassian.plugin.connect.confluence.contenttype;

import java.util.Set;

import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.content.ContentEntityAdapter;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.content.CustomContentManager;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.ContentTypeApiSupport;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.content.custom.BaseCustomContentType;
import com.atlassian.confluence.content.ui.ContentUiSupport;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;


public class ExtensibleContentType extends BaseCustomContentType
{
    private final String contentTypeKey;
    private final String contentTypeName;
    private final ExtensibleContentTypeModuleBean bean;
    private final CustomContentManager customContentManager;
    private final PermissionManager permissionManager;
    private final ApiSupportProvider apiSupportProvider;
    private final ContentEntityAdapter contentEntityAdapter;
    private final com.atlassian.confluence.security.PermissionDelegate permissionDelegate;
    private final ContentUiSupport contentUiSupport;
    private final CustomContentApiSupportParams customContentApiSupportParams;
    private final Set<String> supportedContainerTypes;
    private final Set<String> supportedContainedTypes;

    public ExtensibleContentType(
            String contentTypeKey,
            ExtensibleContentTypeModuleBean bean,
            ContentTypeMapper contentTypeMapper,
            CustomContentManager customContentManager,
            PermissionManager permissionManager,
            ApiSupportProvider apiSupportProvider,
            CustomContentApiSupportParams customContentApiSupportParams)
    {
        super(ContentType.valueOf(contentTypeKey), apiSupportProvider);

        this.bean = bean;
        this.contentTypeKey = contentTypeKey;
        this.contentTypeName = bean.getName().getI18nOrValue();

        this.customContentManager = customContentManager;
        this.permissionManager = permissionManager;
        this.apiSupportProvider = apiSupportProvider;
        this.permissionDelegate = new PermissionDelegate();
        this.contentEntityAdapter = new ExtensibleContentEntityAdapter(contentTypeMapper);
        this.contentUiSupport = new ExtensibleContentTypeUISupport(contentTypeName, bean);
        this.customContentApiSupportParams = customContentApiSupportParams;

        this.supportedContainerTypes = bean.getApiSupport().getSupportedContainerTypes();
        this.supportedContainedTypes = bean.getApiSupport().getSupportedContainedTypes();
    }

    @Override
    public ContentEntityAdapter getContentAdapter()
    {
        return contentEntityAdapter;
    }

    @Override
    public com.atlassian.confluence.security.PermissionDelegate getPermissionDelegate()
    {
        return permissionDelegate;
    }

    @Override
    public ContentUiSupport getContentUiSupport()
    {
        return contentUiSupport;
    }

    @Override
    public ContentTypeApiSupport<CustomContentEntityObject> getApiSupport()
    {
        return new ExtensibleContentTypeSupport(
                contentTypeKey,
                supportedContainerTypes,
                supportedContainedTypes,
                customContentApiSupportParams,
                customContentManager,
                permissionManager,
                apiSupportProvider);
    }
}
