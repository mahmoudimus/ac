package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.impl.service.content.factory.ContentFactory;
import com.atlassian.confluence.content.ContentType;
import com.atlassian.confluence.content.ContentTypeModuleDescriptor;
import com.atlassian.confluence.content.CustomContentManager;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.security.PermissionDelegate;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.module.ModuleFactory;

public class ExtensibleContentTypeModuleDescriptor extends ContentTypeModuleDescriptor
{
    private final String contentTypeKey;
    private final String completeModuleKey;
    private final ExtensibleContentTypeModuleBean bean;
    private final ContentFactory contentFactory;
    private final ContentTypeMapper contentTypeMapper;
    private final ApiSupportProvider apiSupportProvider;
    private final CustomContentManager customContentManager;
    private final CustomContentApiSupportParams customContentApiSupportParams;

    public ExtensibleContentTypeModuleDescriptor(
            String contentTypeKey,
            String completeModuleKey,
            ExtensibleContentTypeModuleBean bean,
            ModuleFactory moduleFactory,
            ContentFactory contentFactory,
            ContentTypeMapper contentTypeMapper,
            ApiSupportProvider apiSupportProvider,
            CustomContentManager customContentManager,
            CustomContentApiSupportParams customContentApiSupportParams)
    {
        super(moduleFactory, apiSupportProvider);
        this.bean = bean;

        this.contentTypeKey = contentTypeKey;
        this.completeModuleKey = completeModuleKey;
        this.contentFactory = contentFactory;
        this.contentTypeMapper = contentTypeMapper;
        this.apiSupportProvider = apiSupportProvider;
        this.customContentManager = customContentManager;
        this.customContentApiSupportParams = customContentApiSupportParams;
    }

    @Override
    protected void loadClass(Plugin plugin, String clazz) throws PluginParseException
    {
        try
        {
            this.moduleClass = plugin.loadClass(clazz, null);
        }
        catch (ClassNotFoundException e)
        {
            throw new PluginParseException("cannot load component class", e);
        }
    }

    @Override
    public ContentType createModule()
    {
        return new ExtensibleContentType(
                contentTypeKey,
                bean,
                contentFactory,
                contentTypeMapper,
                apiSupportProvider,
                customContentManager,
                customContentApiSupportParams);
    }

    @Override
    public String getCompleteKey()
    {
        return completeModuleKey;
    }
}
