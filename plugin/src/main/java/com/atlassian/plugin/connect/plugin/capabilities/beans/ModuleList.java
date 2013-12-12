package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.ConnectModule;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.*;
import com.atlassian.plugin.spring.scanner.ProductFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * This class represents the list of modules in the json descriptor.
 * Every new module type needs to be added here as a private field and annotated with @ConnectModule
 * 
 * The field name will be what appears in the json.
 * 
 * Note: this class does NOT have a builder. Instead the {@link ConnectAddonBean} has a special reflective builder
 * that will handle adding beans to the proper fields in this class by name and type.
 * You can buy me a beer later for that little trick when you realize you don't need to keep updating a builder everytime you add a new type here.
 */
public class ModuleList extends BaseModuleBean
{
    @ConnectModule(WebItemModuleProvider.class)
    private List<WebItemModuleBean> webItems;

    /**
     * @schemaTitle Component Tab Panel
     */
    @ConnectModule (value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraComponentTabPanels;

    /**
     * @schemaTitle Issue Tab Panel
     */
    @ConnectModule (value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraIssueTabPanels;

    @ConnectModule (value = ConnectProjectAdminTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectProjectAdminTabPanelModuleBean> jiraProjectAdminTabPanels;

    /**
     * @schemaTitle Project Tab Panel
     */
    @ConnectModule (value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraProjectTabPanels;

    /**
     * @schemaTitle Version Tab Panel
     */
    @ConnectModule (value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraVersionTabPanels;

    @ConnectModule (value = WorkflowPostFunctionModuleProvider.class, products = {ProductFilter.JIRA})
    private List<WorkflowPostFunctionModuleBean> jiraWorkflowPostFunctions;
    
    @ConnectModule (WebPanelModuleProvider.class)
    private List<WebPanelModuleBean> webPanels;

    /**
     * @schemaTitle General Page
     */
    @ConnectModule (GeneralPageModuleProvider.class)
    private List<ConnectPageModuleBean> generalPages;

    /**
     * @schemaTitle Admin Page
     */
    @ConnectModule (AdminPageModuleProvider.class)
    private List<ConnectPageModuleBean> adminPages;

    @ConnectModule (WebHookModuleProvider.class)
    private List<WebHookModuleBean> webhooks;

    @ConnectModule (value = SearchRequestViewModuleProvider.class, products = {ProductFilter.JIRA})
    private List<SearchRequestViewModuleBean> jiraSearchRequestViews;

    public ModuleList()
    {
        this.jiraComponentTabPanels = newArrayList();
        this.jiraIssueTabPanels = newArrayList();
        this.jiraProjectAdminTabPanels = newArrayList();
        this.jiraProjectTabPanels = newArrayList();
        this.jiraVersionTabPanels = newArrayList();
        this.webItems = newArrayList();
        this.webPanels = newArrayList();
        this.generalPages = newArrayList();
        this.adminPages = newArrayList();
        this.jiraWorkflowPostFunctions = newArrayList();
        this.webhooks = newArrayList();
        this.jiraSearchRequestViews = newArrayList();
    }

    public ModuleList(BaseModuleBeanBuilder builder)
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
        if (null == webItems)
        {
            this.webItems = newArrayList();
        }
        if (null == webPanels)
        {
            this.webPanels = newArrayList();
        }
        if (null == generalPages)
        {
            this.generalPages = newArrayList();
        }
        if (null == adminPages)
        {
            this.adminPages = newArrayList();
        }
        if (null == jiraWorkflowPostFunctions)
        {
            this.jiraWorkflowPostFunctions = newArrayList();
        }
        if (null == webhooks)
        {
            this.webhooks = newArrayList();
        }
        if (null == jiraSearchRequestViews)
        {
            this.jiraSearchRequestViews = newArrayList();
        }
    }

    public List<WebItemModuleBean> getWebItems()
    {
        return webItems;
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

    public List<WebPanelModuleBean> getWebPanels()
    {
        return webPanels;
    }

    public List<WorkflowPostFunctionModuleBean> getJiraWorkflowPostFunctions()
    {
        return jiraWorkflowPostFunctions;
    }

    public List<ConnectPageModuleBean> getGeneralPages()
    {
        return generalPages;
    }

    public List<ConnectPageModuleBean> getAdminPages()
    {
        return adminPages;
    }

    public List<WebHookModuleBean> getWebhooks()
    {
        return webhooks;
    }

    public List<SearchRequestViewModuleBean> getJiraSearchRequestViews()
    {
        return jiraSearchRequestViews;
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ModuleList))
        {
            return false;
        }

        ModuleList other = (ModuleList) otherObj;

        return new EqualsBuilder()
                .append(adminPages, other.adminPages)
                .append(generalPages, other.generalPages)
                .append(jiraComponentTabPanels, other.jiraComponentTabPanels)
                .append(jiraIssueTabPanels, other.jiraIssueTabPanels)
                .append(jiraProjectAdminTabPanels, other.jiraProjectAdminTabPanels)
                .append(jiraProjectTabPanels, other.jiraProjectTabPanels)
                .append(jiraSearchRequestViews, other.jiraSearchRequestViews)
                .append(jiraVersionTabPanels, other.jiraVersionTabPanels)
                .append(jiraWorkflowPostFunctions, other.jiraWorkflowPostFunctions)
                .append(webhooks, other.webhooks)
                .append(webItems, other.webItems)
                .build();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(29, 37)
                .append(adminPages)
                .append(generalPages)
                .append(jiraComponentTabPanels)
                .append(jiraIssueTabPanels)
                .append(jiraProjectAdminTabPanels)
                .append(jiraProjectTabPanels)
                .append(jiraSearchRequestViews)
                .append(jiraVersionTabPanels)
                .append(jiraWorkflowPostFunctions)
                .append(webhooks)
                .append(webItems)
                .build();
    }
}
