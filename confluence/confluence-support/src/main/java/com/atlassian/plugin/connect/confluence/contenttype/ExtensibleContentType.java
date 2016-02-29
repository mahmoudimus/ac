package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.service.content.ContentService;
import com.atlassian.confluence.api.service.pagination.PaginationService;
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

import java.util.Set;


public class ExtensibleContentType extends BaseCustomContentType {
    private final String contentTypeKey;
    private final String contentTypeName;
    private final ContentEntityAdapter contentEntityAdapter;
    private final PaginationService paginationService;
    private final ContentService contentService;
    private final PermissionDelegate permissionDelegate;
    private final ContentUiSupport contentUiSupport;
    private final CustomContentApiSupportParams customContentApiSupportParams;
    private final Set<String> supportedContainerTypes;
    private final Set<String> supportedContainedTypes;

    public ExtensibleContentType(
            String contentTypeKey,
            ExtensibleContentTypeModuleBean bean,
            PermissionManager permissionManager,
            PaginationService paginationService,
            ContentService contentService,
            CustomContentApiSupportParams customContentApiSupportParams) {

        super(ContentType.valueOf(contentTypeKey), customContentApiSupportParams.getProvider());

        this.contentTypeKey = contentTypeKey;
        this.contentTypeName = bean.getName().getI18nOrValue();

        this.permissionDelegate = new PermissionDelegate(permissionManager);
        this.paginationService = paginationService;
        this.contentService = contentService;
        this.contentEntityAdapter = new ExtensibleContentEntityAdapter(bean);
        this.contentUiSupport = new ExtensibleContentTypeUISupport(contentTypeName, bean);
        this.customContentApiSupportParams = customContentApiSupportParams;

        this.supportedContainerTypes = bean.getApiSupport().getSupportedContainerTypes();
        this.supportedContainedTypes = bean.getApiSupport().getSupportedContainedTypes();
    }

    public String getContentTypeKey() {
        return contentTypeKey;
    }

    public Set<String> getSupportedContainerTypes() {
        return supportedContainerTypes;
    }

    public Set<String> getSupportedContainedTypes() {
        return supportedContainedTypes;
    }

    @Override
    public ContentEntityAdapter getContentAdapter() {
        return contentEntityAdapter;
    }

    @Override
    public PermissionDelegate getPermissionDelegate() {
        return permissionDelegate;
    }

    @Override
    public ContentUiSupport getContentUiSupport() {
        return contentUiSupport;
    }

    @Override
    public ContentTypeApiSupport<CustomContentEntityObject> getApiSupport() {
        return new ExtensibleContentTypeSupport(
                this,
                customContentApiSupportParams,
                paginationService,
                contentService);
    }
}