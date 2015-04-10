package com.atlassian.plugin.connect.modules.beans;


import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.DashboardItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import org.apache.commons.lang3.StringUtils;

public class DashboardItemModuleBean extends BeanWithKeyAndParamsAndConditions
{
    @Required
    private I18nProperty title;
    @Required
    private I18nProperty description;
    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String url;
    @CommonSchemaAttributes (defaultValue = "false")
    private Boolean configurable;
    private IconBean icon;

    public DashboardItemModuleBean()
    {
        super();
        this.title = I18nProperty.empty();
        this.description = I18nProperty.empty();
        this.url = StringUtils.EMPTY;
        this.configurable = false;
    }


    public DashboardItemModuleBean(DashboardItemModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static DashboardItemModuleBeanBuilder newDashboardItemModuleBean()
    {
        return new DashboardItemModuleBeanBuilder();
    }

    public I18nProperty getTitle()
    {
        return title;
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public String getUrl()
    {
        return url;
    }

    public Boolean isConfigurable()
    {
        return configurable;
    }

    public IconBean getIcon()
    {
        return icon;
    }
}
