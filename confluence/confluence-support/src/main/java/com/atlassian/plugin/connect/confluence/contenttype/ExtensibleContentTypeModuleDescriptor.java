package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.service.content.ContentService;
import com.atlassian.confluence.api.service.pagination.PaginationService;
import com.atlassian.confluence.content.ContentType;
import com.atlassian.confluence.content.ContentTypeModuleDescriptor;
import com.atlassian.confluence.content.CustomContentManager;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.module.ModuleFactory;

public class ExtensibleContentTypeModuleDescriptor extends ContentTypeModuleDescriptor {
    private final String contentTypeKey;
    private final ExtensibleContentTypeModuleBean bean;
    private final CustomContentManager customContentManager;
    private final PermissionManager permissionManager;
    private final ApiSupportProvider apiSupportProvider;
    private final CustomContentApiSupportParams customContentApiSupportParams;
    private final PaginationService paginationService;
    private final ContentService contentService;

    public ExtensibleContentTypeModuleDescriptor(
            String contentTypeKey,
            ExtensibleContentTypeModuleBean bean,
            ModuleFactory moduleFactory,
            CustomContentManager customContentManager,
            PermissionManager permissionManager,
            PaginationService paginationService,
            ContentService contentService,
            ApiSupportProvider apiSupportProvider,
            CustomContentApiSupportParams customContentApiSupportParams) {
        super(moduleFactory, apiSupportProvider);
        this.bean = bean;

        this.contentTypeKey = contentTypeKey;
        this.customContentManager = customContentManager;
        this.permissionManager = permissionManager;
        this.paginationService = paginationService;
        this.contentService = contentService;
        this.apiSupportProvider = apiSupportProvider;
        this.customContentApiSupportParams = customContentApiSupportParams;
    }

    @Override
    protected void loadClass(Plugin plugin, String clazz) throws PluginParseException {
        try {
            this.moduleClass = plugin.loadClass(clazz, null);
        } catch (ClassNotFoundException e) {
            throw new PluginParseException("cannot load component class", e);
        }
    }

    @Override
    public ContentType createModule() {
        return new ExtensibleContentType(
                contentTypeKey,
                bean,
                customContentManager,
                permissionManager,
                paginationService,
                contentService,
                apiSupportProvider,
                customContentApiSupportParams);
    }

    @Override
    public String getCompleteKey() {
        return contentTypeKey;
    }
}
