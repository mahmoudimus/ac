package com.atlassian.plugin.connect.modules.beans.builder.jira;

import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ModuleListBuilder;
import com.atlassian.plugin.connect.modules.beans.jira.JiraModuleList;

public abstract class JiraModuleListBuilder<T extends JiraModuleListBuilder,
        M extends JiraModuleList> extends ModuleListBuilder<T, M>
{

    @Override
    protected M createEmpty()
    {
        return (M) new JiraModuleList(); // TODO: fix once we removed JiraConfluenceModuleListBuilder
    }

    @Override
    public M build()
    {
        return modules;
    }

    public T withWebItems(WebItemModuleBean... beans)
    {
        return super.withWebItems(beans);
    }

    public T withWebPanels(WebPanelModuleBean... beans)
    {
        return super.withWebPanels(beans);
    }

    public T withWebSections(WebSectionModuleBean... beans)
    {
        return super.withWebSections(beans);
    }

    public T withWebHooks(WebHookModuleBean... beans)
    {
        return super.withWebHooks(beans);
    }

    public T withGeneralPages(ConnectPageModuleBean... beans)
    {
        return super.withGeneralPages(beans);
    }

    public T withAdminPages(ConnectPageModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("adminPages", beans);
    }

    public T withProfilePages(ConnectPageModuleBean... beans)
    {
        return super.withProfilePages(beans);
    }

    public T withConfigurePage(ConnectPageModuleBean bean)
    {
        return super.withConfigurePage(bean);
    }


    public T withJiraComponentTabPanels(ConnectTabPanelModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraComponentTabPanels", beans);
    }

    public T withJiraIssueTabPanels(ConnectTabPanelModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraIssueTabPanels", beans);
    }

    public T withJiraProjectAdminTabPanels(ConnectProjectAdminTabPanelModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraProjectAdminTabPanels", beans);
    }

    public T withJiraProjectTabTabPanels(ConnectTabPanelModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraProjectTabTabPanels", beans);
    }

    public T withJiraVersionTabPanels(ConnectTabPanelModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraVersionTabPanels", beans);
    }

    public T withJiraProfileTabPanels(ConnectTabPanelModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraProfileTabPanels", beans);
    }

    public T withJiraSearchRequestViews(SearchRequestViewModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraSearchRequestViews", beans);
    }

    public T withJiraWorkflowPostFunctions(WorkflowPostFunctionModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraWorkflowPostFunctions", beans);
    }

    public T withJiraEntityProperties(EntityPropertyModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraEntityProperties", beans);
    }

    public T withJiraReports(ReportModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("jiraReports", beans);
    }

}
