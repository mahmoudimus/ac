package com.atlassian.plugin.connect.modules.beans.jira;

import java.util.List;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.JiraConfluenceModuleListBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.jira.JiraModuleListBuilder;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class JiraModuleList extends ModuleList
{
    /**
     * The Component Tab Panel module allows you to add new tabs to the 'Browse Component' page.
     *
     * @schemaTitle Component Tab Panel
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraComponentTabPanels;
    
    /**
     * The Issue Tab Panel module allows you to add new tabs to the 'Browse Component' page.
     *
     * @schemaTitle Issue Tab Panel
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraIssueTabPanels;
    
    /**
     * The Project Admin Tab Panel module allows you to add new panels to the 'Project Admin' page.
     *
     * @schemaTitle Issue Tab Panel
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectProjectAdminTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectProjectAdminTabPanelModuleBean> jiraProjectAdminTabPanels;
    
    /**
     * The Project Tab Panel module allows you to add new panels to the 'Project' page.
     *
     * @schemaTitle Project Tab Panel
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraProjectTabPanels;
    
    /**
     * The Version Tab Panel module allows you to add new panels to the 'Browse Version' page.
     *
     * @schemaTitle Version Tab Panel
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraVersionTabPanels;
    
    /**
     * The User Profile Tab Panel module allows you to add new tabs to the 'Browse Component' page.
     *
     * @schemaTitle User Profile Tab Panel
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraProfileTabPanels;
    
    /**
     * The Search Request View is used to display different representations of search results in the issue navigator.
     * They will be displayed as a link in the `Export` toolbar menu.
     *
     * @schemaTitle Search Request View
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.SearchRequestViewModuleProvider", products = {ProductFilter.JIRA})
    private List<SearchRequestViewModuleBean> jiraSearchRequestViews;
    
    /**
     * Workflow post functions execute after the workflow transition is executed
     *
     * @schemaTitle Workflow Post Function
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultWorkflowPostFunctionModuleProvider", products = {ProductFilter.JIRA})
    private List<WorkflowPostFunctionModuleBean> jiraWorkflowPostFunctions;
    
    /**
     * The Entity Property are add-on key/value stories in certain JIRA objects, such as issues and projects.
     * @schemaTitle Entity Property
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.EntityPropertyModuleProvider", products = {ProductFilter.JIRA})
    private List<EntityPropertyModuleBean> jiraEntityProperties;
    
    /**
     * Add new report modules to JIRA projects.
     * @schemaTitle Report
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ReportModuleProvider", products = {ProductFilter.JIRA})
    private List<ReportModuleBean> jiraReports;

    public JiraModuleList()
    {
        this.jiraComponentTabPanels = newArrayList();
        this.jiraIssueTabPanels = newArrayList();
        this.jiraProfileTabPanels = newArrayList();
        this.jiraProjectAdminTabPanels = newArrayList();
        this.jiraProjectTabPanels = newArrayList();
        this.jiraSearchRequestViews = newArrayList();
        this.jiraVersionTabPanels = newArrayList();
        this.jiraWorkflowPostFunctions = newArrayList();
        this.jiraEntityProperties = newArrayList();
        this.jiraReports = newArrayList();
    }

    public JiraModuleList(BaseModuleBeanBuilder builder)
    {
        super(builder);

        if (null == jiraComponentTabPanels)
        {
            this.jiraComponentTabPanels = newArrayList();
        }
        if (null == jiraIssueTabPanels)
        {
            this.jiraIssueTabPanels = newArrayList();
        }
        if (null == jiraProjectAdminTabPanels)
        {
            this.jiraProjectAdminTabPanels = newArrayList();
        }
        if (null == jiraProjectTabPanels)
        {
            this.jiraProjectTabPanels = newArrayList();
        }
        if (null == jiraVersionTabPanels)
        {
            this.jiraVersionTabPanels = newArrayList();
        }
        if (null == jiraProfileTabPanels)
        {
            this.jiraProfileTabPanels = newArrayList();
        }
        if (null == jiraEntityProperties)
        {
            this.jiraEntityProperties = newArrayList();
        }
        if (null == jiraWorkflowPostFunctions)
        {
            this.jiraWorkflowPostFunctions = newArrayList();
        }
        if (null == jiraSearchRequestViews)
        {
            this.jiraSearchRequestViews = newArrayList();
        }
        if (null == jiraReports)
        {
            this.jiraReports = newArrayList();
        }
    }

    public List<ConnectTabPanelModuleBean> getJiraComponentTabPanels()
    {
        return jiraComponentTabPanels;
    }

    public List<ConnectTabPanelModuleBean> getJiraIssueTabPanels()
    {
        return jiraIssueTabPanels;
    }

    public List<ConnectProjectAdminTabPanelModuleBean> getJiraProjectAdminTabPanels()
    {
        return jiraProjectAdminTabPanels;
    }

    public List<ConnectTabPanelModuleBean> getJiraProjectTabPanels()
    {
        return jiraProjectTabPanels;
    }

    public List<ConnectTabPanelModuleBean> getJiraVersionTabPanels()
    {
        return jiraVersionTabPanels;
    }

    public List<ConnectTabPanelModuleBean> getJiraProfileTabPanels()
    {
        return jiraProfileTabPanels;
    }

    public List<WorkflowPostFunctionModuleBean> getJiraWorkflowPostFunctions()
    {
        return jiraWorkflowPostFunctions;
    }

    public List<EntityPropertyModuleBean> getJiraEntityProperties()
    {
        return jiraEntityProperties;
    }

    public List<ReportModuleBean> getJiraReports()
    {
        return jiraReports;
    }

    public List<SearchRequestViewModuleBean> getJiraSearchRequestViews()
    {
        return jiraSearchRequestViews;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof JiraModuleList))
        {
            return false;
        }

        JiraModuleList other = (JiraModuleList) otherObj;

        return new EqualsBuilder()
                .appendSuper(super.equals(otherObj))
                .append(jiraComponentTabPanels, other.jiraComponentTabPanels)
                .append(jiraIssueTabPanels, other.jiraIssueTabPanels)
                .append(jiraProfileTabPanels, other.jiraProfileTabPanels)
                .append(jiraProjectAdminTabPanels, other.jiraProjectAdminTabPanels)
                .append(jiraProjectTabPanels, other.jiraProjectTabPanels)
                .append(jiraSearchRequestViews, other.jiraSearchRequestViews)
                .append(jiraVersionTabPanels, other.jiraVersionTabPanels)
                .append(jiraWorkflowPostFunctions, other.jiraWorkflowPostFunctions)
                .append(jiraEntityProperties, other.jiraEntityProperties)
                .append(jiraReports, other.jiraReports)
                .build();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(29, 37)
                .append(jiraComponentTabPanels)
                .append(jiraIssueTabPanels)
                .append(jiraProfileTabPanels)
                .append(jiraProjectAdminTabPanels)
                .append(jiraProjectTabPanels)
                .append(jiraSearchRequestViews)
                .append(jiraVersionTabPanels)
                .append(jiraWorkflowPostFunctions)
                .append(jiraEntityProperties)
                .append(jiraReports)
                .build();
    }

    public static JiraModuleListBuilder newModuleList()
    {
        return new JiraModuleListBuilder();
    }

//    public static JiraModuleListBuilder newModuleList(JiraModuleList defaultList)
//    {
//        return new JiraModuleListBuilder(defaultList);
//    }

}
