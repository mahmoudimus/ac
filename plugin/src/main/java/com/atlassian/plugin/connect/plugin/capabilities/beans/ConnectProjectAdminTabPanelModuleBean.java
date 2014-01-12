package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectProjectAdminTabPanelModuleBeanBuilder;

/**
 * Project administration tab panel modules add a new tab and corresponding page to the left hand menu on the JIRA
 * project administration page.
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#PRJ_ADMIN_PAGE_EXAMPLE}
 *
 * @schemaTitle Project Admin Tab Panel
 */
public class ConnectProjectAdminTabPanelModuleBean extends ConnectTabPanelModuleBean
{
    private static final String PROJECT_CONFIG_TAB_LOCATION_PREFIX = "atl.jira.proj.config/";
    private String location;

    public ConnectProjectAdminTabPanelModuleBean() {}

    public ConnectProjectAdminTabPanelModuleBean(ConnectProjectAdminTabPanelModuleBeanBuilder builder)
    {
        super(builder);
        if (null == location)
        {
            this.location = "";
        }
    }

    public static ConnectProjectAdminTabPanelModuleBeanBuilder newProjectAdminTabPanelBean()
    {
        return new ConnectProjectAdminTabPanelModuleBeanBuilder();
    }

    public static ConnectProjectAdminTabPanelModuleBeanBuilder newProjectAdminTabPanelBean(ConnectProjectAdminTabPanelModuleBean defaultBean)
    {
        return new ConnectProjectAdminTabPanelModuleBeanBuilder(defaultBean);
    }

    public String getLocation()
    {
        return location;
    }

    public String getAbsoluteLocation()
    {
        return PROJECT_CONFIG_TAB_LOCATION_PREFIX + location;
    }

}
