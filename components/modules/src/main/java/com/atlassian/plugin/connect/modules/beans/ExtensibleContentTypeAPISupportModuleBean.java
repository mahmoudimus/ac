package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeAPISupportModuleBeanBuilder;

public class ExtensibleContentTypeAPISupportModuleBean extends BaseModuleBean
{
    private String createUrl;

    public ExtensibleContentTypeAPISupportModuleBean(ExtensibleContentTypeAPISupportModuleBeanBuilder builder)
    {
        super(builder);
    }

    public String getCreateUrl()
    {
        return createUrl;
    }
}
