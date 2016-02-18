package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.UISupportBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

@SchemaDefinition("uiSupport")
public class UISupportBean extends BaseModuleBean
{
    private String viewComponent;

    @Required
    private I18nProperty typeName;

    private IconsBean icons;

    public UISupportBean()
    {
        this(new UISupportBeanBuilder());
    }

    public UISupportBean(UISupportBeanBuilder builder)
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

    public IconsBean getIcons()
    {
        return icons;
    }
}
