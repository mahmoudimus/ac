package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.DashboardItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * Dashboard items allow add-on to display a summary information data on the dashboard. Each dashboard-item can be configured
 * to display information relevant to a particular user.
 *
 *#### Example
 * For a full add-on example, see [dashboard item example add-on](bitbucket.org/atlassianlabs/atlassian-connect-jira-dashboard-item-example).
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#DASHBOARD_ITEM_EXAMPLE}
 * @schemaTitle Dashboard Item
 * @since 1.0
 */
public class DashboardItemModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * Description of the dashboard item. This will be displayed for a user in the directory.
     */
    @Required
    private I18nProperty description;

    /**
     * The URL of the service which will render the dashboard item. Following context parameters are supported in
     * url.
     *
     * * `dashboard.id` unique id of the dashboard on which the item is rendered. This parameter is passed only on default view
     * * `dashboardItem.id` unique id of the dashboard item which is rendered. This parameter is passed only on default view
     * * `dashboardItem.key` key of the dashboard item. This parameter is passed in both: default and directory view
     * * `dashboardItem.viewType` type of the view in which dashboard item is displayed. Default (for dashboard) and directory. This list may be extended
     */
    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String url;

    /**
     * Specify if the dashboard-item is configurable or not. Configurable dashboard items should render configuration
     * view if there is no configuration stored for the dashboard item. [Dashboard item properties](scopes/jira-rest-scopes.html) can
     * be used for configuration storage.
     *
     * In addition, configurable dashboard items should register a javascript callback for `edit click`.
     *
     *     AP.require(['jira'], function (jira) {
     *        jira.DashboardItem.onDashboardItemEdit(function() {
     *           // render dashboard item configuration now
     *        });
     *     });
     *
     * It is a common case to give users ability to set the name of the dashboard item. This can be achieved with a following
     * JS code:
     *
     *     AP.require(['jira'], function(jira) {
     *        jira.setDashboardItemTitle("Setting title works");
     *     });
     */
    @CommonSchemaAttributes (defaultValue = "false")
    private Boolean configurable;

    /**
     * URI of the dashboard item thumbnail which is displayed in the directory.
     */
    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String thumbnailUrl;

    public DashboardItemModuleBean()
    {
        super();
        this.description = I18nProperty.empty();
        this.url = "";
        this.configurable = false;
        this.thumbnailUrl = "";
    }


    public DashboardItemModuleBean(DashboardItemModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static DashboardItemModuleBeanBuilder newBuilder()
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
