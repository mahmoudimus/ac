package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectProjectAdminTabPanelModuleBeanBuilder;

/**
 * Project administration tab panel modules add a new tab and corresponding page to the left hand menu on the JIRA
 * project administration page.
 *
 *#### Example
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#PRJ_ADMIN_PAGE_EXAMPLE}
 * @schemaTitle Project Admin Tab Panel
 */
@SchemaDefinition("projectAdminTabPanel")
public class ConnectProjectAdminTabPanelModuleBean extends ConnectTabPanelModuleBean
{
    private static final String PROJECT_CONFIG_TAB_LOCATION_PREFIX = "atl.jira.proj.config/";

    /**
     * The name of the group of tabs in the project configuration where the tab panel should appear.
     * The following are accepted values.
     *
     * * `projectgroup1`
     * * `projectgroup2`
     * * `projectgroup3`
     * * `projectgroup4`
     *
     *See [Project Configuration Locations](https://developer.atlassian.com/jiradev/jira-architecture/web-fragments/project-configuration-locations#ProjectConfigurationLocations-AddingNewItemstoExistingWebSections) for details.
     */

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
