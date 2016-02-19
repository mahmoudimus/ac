package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ContentType;
import com.atlassian.confluence.content.ContentTypeModuleDescriptor;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.module.ModuleFactory;

public class ExtensibleContentTypeModuleDescriptor extends ContentTypeModuleDescriptor
{
    private final String contentTypeKey;
    private final ExtensibleContentTypeModuleBean bean;
    private final ContentTypeMapper contentTypeMapper;
    private final ApiSupportProvider apiSupportProvider;
    private final CustomContentApiSupportParams customContentApiSupportParams;

    public ExtensibleContentTypeModuleDescriptor(
            String contentTypeKey,
            ExtensibleContentTypeModuleBean bean,
            ModuleFactory moduleFactory,
            ContentTypeMapper contentTypeMapper,
            ApiSupportProvider apiSupportProvider,
            CustomContentApiSupportParams customContentApiSupportParams)
    {
        super(moduleFactory, apiSupportProvider);
        this.bean = bean;

        this.contentTypeKey = contentTypeKey;
        this.contentTypeMapper = contentTypeMapper;
        this.apiSupportProvider = apiSupportProvider;
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
                contentTypeMapper,
                apiSupportProvider,
                customContentApiSupportParams);
    }

    @Override
    public String getCompleteKey()
    {
        return contentTypeKey;
    }
}
