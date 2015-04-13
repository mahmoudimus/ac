package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.DashboardItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * Dashboard items allow add-on to display a summary information data on the dashboard. Each dashboard-item can be configured
 * to display information relevant to a particular user.
 */
public class DashboardItemModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * Description of the dashboard item. This will be displayed for a user in the directory.
     */
    @Required
    private I18nProperty description;

    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String url;

    /**
     * FLag specifying if the dashboard-item is configurable. In order for the dashboard item to be configurable
     */
    @CommonSchemaAttributes (defaultValue = "false")
    private Boolean configurable;

    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String thumbnailUrl;

    public DashboardItemModuleBean()
    {
        super();
        this.description = I18nProperty.empty();
        this.url = StringUtils.EMPTY;
        this.configurable = false;
        this.thumbnailUrl = StringUtils.EMPTY;
    }


    public DashboardItemModuleBean(DashboardItemModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static DashboardItemModuleBeanBuilder newDashboardItemModuleBean()
    {
        return new DashboardItemModuleBeanBuilder();
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

    public String getThumbnailUrl()
    {
        return thumbnailUrl;
    }
}
