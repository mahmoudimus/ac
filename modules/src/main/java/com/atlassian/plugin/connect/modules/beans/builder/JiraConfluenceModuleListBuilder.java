package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.jira.JiraModuleListBuilder;

public class JiraConfluenceModuleListBuilder<T extends JiraConfluenceModuleListBuilder,
        M extends JiraConfluenceModuleList> extends JiraModuleListBuilder<T, M>
{

    @Override
    protected M createEmpty()
    {
        return (M) new JiraConfluenceModuleList(); // TODO: fix once we removed JiraConfluenceModuleListBuilder
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
        return super.withAdminPages(beans);
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
        return super.withJiraComponentTabPanels(beans);
    }

    public T withJiraIssueTabPanels(ConnectTabPanelModuleBean... beans)
    {
        return super.withJiraIssueTabPanels(beans);
    }

    public T withJiraProjectAdminTabPanels(ConnectProjectAdminTabPanelModuleBean... beans)
    {
        return super.withJiraProjectAdminTabPanels(beans);
    }

    public T withJiraProjectTabTabPanels(ConnectTabPanelModuleBean... beans)
    {
        return super.withJiraProjectTabTabPanels(beans);
    }

    public T withJiraVersionTabPanels(ConnectTabPanelModuleBean... beans)
    {
        return super.withJiraVersionTabPanels(beans);
    }

    public T withJiraProfileTabPanels(ConnectTabPanelModuleBean... beans)
    {
        return super.withJiraProfileTabPanels(beans);
    }

    public T withJiraSearchRequestViews(SearchRequestViewModuleBean... beans)
    {
        return super.withJiraSearchRequestViews(beans);
    }

    public T withJiraWorkflowPostFunctions(WorkflowPostFunctionModuleBean... beans)
    {
        return super.withJiraWorkflowPostFunctions(beans);
    }

    public T withJiraEntityProperties(EntityPropertyModuleBean... beans)
    {
        return super.withJiraEntityProperties(beans);
    }

    public T withJiraReports(ReportModuleBean... beans)
    {
        return super.withJiraReports(beans);
    }

    public T withDynamicContentMacros(DynamicContentMacroModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("dynamicContentMacros", beans);
    }

    public T withSpaceToolsTabs(SpaceToolsTabModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("spaceToolsTabs", beans);
    }

    public T withStaticContentMacros(StaticContentMacroModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("staticContentMacros", beans);
    }

}
