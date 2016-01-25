package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeUISupportModuleBeanBuilder;

public class ExtensibleContentTypeUISupportModuleBean extends BaseModuleBean
{
    private String viewComponent;

    @Required
    private I18nProperty typeName;

    private ExtensibleContentTypeIconsModuleBean icons;

    public ExtensibleContentTypeUISupportModuleBean()
    {
        super(new ExtensibleContentTypeUISupportModuleBeanBuilder());
        initialise();
    }

    public ExtensibleContentTypeUISupportModuleBean(ExtensibleContentTypeUISupportModuleBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {

    }

    public String getViewComponent()
    {
        return viewComponent;
    }

    public I18nProperty getTypeName()
    {
        return typeName;
    }

    public ExtensibleContentTypeIconsModuleBean getIcons()
    {
        return icons;
    }
}
