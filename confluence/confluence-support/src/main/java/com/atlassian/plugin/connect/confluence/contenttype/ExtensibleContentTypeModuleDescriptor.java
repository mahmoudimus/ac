package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.service.content.ContentService;
import com.atlassian.confluence.api.service.pagination.PaginationService;
import com.atlassian.confluence.content.ContentType;
import com.atlassian.confluence.content.ContentTypeModuleDescriptor;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.module.ModuleFactory;

public class ExtensibleContentTypeModuleDescriptor extends ContentTypeModuleDescriptor {
    private final ExtensibleContentTypeModuleBean bean;
    private final PermissionManager permissionManager;
    private final CustomContentApiSupportParams customContentApiSupportParams;
    private final PaginationService paginationService;
    private final ContentService contentService;

    public ExtensibleContentTypeModuleDescriptor(
            ExtensibleContentTypeModuleBean bean,
            ModuleFactory moduleFactory,
            PermissionManager permissionManager,
            PaginationService paginationService,
            ContentService contentService,
            CustomContentApiSupportParams customContentApiSupportParams) {

        super(moduleFactory, customContentApiSupportParams.getProvider());
        this.bean = bean;

        this.permissionManager = permissionManager;
        this.paginationService = paginationService;
        this.contentService = contentService;
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
                getCompleteKey(),
                bean,
                permissionManager,
                paginationService,
                contentService,
                customContentApiSupportParams);
    }
}
