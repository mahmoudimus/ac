package com.atlassian.plugin.connect.jira;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;

import java.util.List;

/**
 * A container class used for generation of JSON schema for JIRA modules.
 */
@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class JiraModuleList extends BaseModuleBean
{

    /**
     * The Issue Tab Panel module allows you to add new tabs to the 'Browse Component' page.
     */
    private List<ConnectTabPanelModuleBean> jiraIssueTabPanels;

    /**
     * The Project Admin Tab Panel module allows you to add new panels to the 'Project Admin' page.
     */
    private List<ConnectProjectAdminTabPanelModuleBean> jiraProjectAdminTabPanels;

    /**
     * The Project Tab Panel module allows you to add new panels to the 'Project' page.
     */
    private List<ConnectTabPanelModuleBean> jiraProjectTabPanels;

    /**
     * The User Profile Tab Panel module allows you to add new tabs to the 'Browse Component' page.
     */
    private List<ConnectTabPanelModuleBean> jiraProfileTabPanels;

    /**
     * The Search Request View is used to display different representations of search results in the issue navigator.
     * They will be displayed as a link in the `Export` toolbar menu.
     */
    private List<SearchRequestViewModuleBean> jiraSearchRequestViews;

    /**
     * Workflow post functions execute after the workflow transition is executed
     */
    private List<WorkflowPostFunctionModuleBean> jiraWorkflowPostFunctions;

    /**
     * The Entity Property are add-on key/value stories in certain JIRA objects, such as issues and projects.
     */
    private List<EntityPropertyModuleBean> jiraEntityProperties;

    /**
     * Add new report modules to JIRA projects.
     */
    private List<ReportModuleBean> jiraReports;

    /**
     * Add new dashboard item to JIRA.
     */
    private List<DashboardItemModuleBean> jiraDashboardItems;

    /**
     * Add global permission to JIRA.
     */
    private List<GlobalPermissionModuleBean> jiraGlobalPermissions;

    /**
     * Add project permission to JIRA.
     */
    private List<ProjectPermissionModuleBean> jiraProjectPermissions;

    /**
     * Add remote issue field to JIRA
     */
    private List<ConnectFieldModuleBean> jiraIssueFields;

    private JiraModuleList()
    {
    }
}
