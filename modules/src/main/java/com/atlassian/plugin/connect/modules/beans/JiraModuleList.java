package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedListWithType;
import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class JiraModuleList extends BaseModuleBean
{


    /**
     * The Issue Tab Panel module allows you to add new tabs to the 'Browse Component' page.
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.ConnectTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraIssueTabPanels;

    /**
     * The Project Admin Tab Panel module allows you to add new panels to the 'Project Admin' page.
     *
     * @schemaTitle Project Admin Tab Panel
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.ConnectProjectAdminTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectProjectAdminTabPanelModuleBean> jiraProjectAdminTabPanels;

    /**
     * The Project Tab Panel module allows you to add new panels to the 'Project' page.
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.ConnectTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraProjectTabPanels;

    /**
     * The User Profile Tab Panel module allows you to add new tabs to the 'Browse Component' page.
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.ConnectTabPanelModuleProvider", products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraProfileTabPanels;

    /**
     * The Search Request View is used to display different representations of search results in the issue navigator.
     * They will be displayed as a link in the `Export` toolbar menu.
     *
     * @schemaTitle Search Request View
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.SearchRequestViewModuleProvider", products = {ProductFilter.JIRA})
    private List<SearchRequestViewModuleBean> jiraSearchRequestViews;

    /**
     * Workflow post functions execute after the workflow transition is executed
     *
     * @schemaTitle Workflow Post Function
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.DefaultWorkflowPostFunctionModuleProvider", products = {ProductFilter.JIRA})
    private List<WorkflowPostFunctionModuleBean> jiraWorkflowPostFunctions;

    /**
     * The Entity Property are add-on key/value stories in certain JIRA objects, such as issues and projects.
     * @schemaTitle Entity Property
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.EntityPropertyModuleProvider", products = {ProductFilter.JIRA})
    private List<EntityPropertyModuleBean> jiraEntityProperties;

    /**
     * Add new report modules to JIRA projects.
     * @schemaTitle Report
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.ReportModuleProvider", products = {ProductFilter.JIRA})
    private List<ReportModuleBean> jiraReports;

    /**
     * Add new dashboard item to JIRA.
     * @schemaTitle Dashboard Item
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.jira.capabilities.provider.DashboardItemModuleProvider", products = {ProductFilter.JIRA})
    private List<DashboardItemModuleBean> jiraDashboardItems;



    public JiraModuleList()
    {
        this.jiraIssueTabPanels = newArrayList();
        this.jiraProfileTabPanels = newArrayList();
        this.jiraProjectAdminTabPanels = newArrayList();
        this.jiraProjectTabPanels = newArrayList();
        this.jiraSearchRequestViews = newArrayList();
        this.jiraWorkflowPostFunctions = newArrayList();
        this.jiraEntityProperties = newArrayList();
        this.jiraReports = newArrayList();
        this.jiraDashboardItems = newArrayList();
    }


    public JiraModuleList(BaseModuleBeanBuilder builder)
    {
        super(builder);

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
        if (null == jiraDashboardItems)
        {
            this.jiraDashboardItems = newArrayList();
        }
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

    public List<DashboardItemModuleBean> getJiraDashboardItems()
    {
        return jiraDashboardItems;
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
                .append(jiraIssueTabPanels, other.jiraIssueTabPanels)
                .append(jiraProfileTabPanels, other.jiraProfileTabPanels)
                .append(jiraProjectAdminTabPanels, other.jiraProjectAdminTabPanels)
                .append(jiraProjectTabPanels, other.jiraProjectTabPanels)
                .append(jiraSearchRequestViews, other.jiraSearchRequestViews)
                .append(jiraWorkflowPostFunctions, other.jiraWorkflowPostFunctions)
                .append(jiraEntityProperties, other.jiraEntityProperties)
                .append(jiraReports, other.jiraReports)
                .append(jiraDashboardItems, other.jiraDashboardItems)
                .build();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(29, 37)
                .append(jiraIssueTabPanels)
                .append(jiraProfileTabPanels)
                .append(jiraProjectAdminTabPanels)
                .append(jiraProjectTabPanels)
                .append(jiraSearchRequestViews)
                .append(jiraWorkflowPostFunctions)
                .append(jiraEntityProperties)
                .append(jiraReports)
                .append(jiraDashboardItems)
                .build();
    }

    public boolean isEmpty()
    {
        for (Field field : getClass().getDeclaredFields())
        {
            if (field.isAnnotationPresent(ConnectModule.class))
            {
                try
                {
                    ConnectModule anno = field.getAnnotation(ConnectModule.class);
                    field.setAccessible(true);

                    Type fieldType = field.getGenericType();

                    List<? extends ModuleBean> beanList;

                    if (isParameterizedListWithType(fieldType, ModuleBean.class))
                    {
                        beanList = (List<? extends ModuleBean>) field.get(this);
                    }
                    else
                    {
                        ModuleBean moduleBean = (ModuleBean) field.get(this);
                        beanList = moduleBean == null ? Collections.<ModuleBean>emptyList() : newArrayList(moduleBean);
                    }

                    if(!beanList.isEmpty())
                    {
                        return false;
                    }

                }
                catch (IllegalAccessException e)
                {
                    //ignore. this should never happen
                }
            }
        }

        return true;
    }


}
