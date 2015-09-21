package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;

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

    private JiraModuleList()
    {
    }
}
