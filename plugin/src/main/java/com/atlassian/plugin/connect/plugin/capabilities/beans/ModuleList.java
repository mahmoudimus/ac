package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.ConnectModule;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.spring.scanner.ProductFilter;
import com.atlassian.plugin.connect.plugin.capabilities.provider.*;

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
    @ConnectModule (WebItemModuleProvider.class)
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

    /**
     * @schemaTitle User Profile Tab Panel
     */
    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraProfileTabPanels;

    @CapabilityModuleProvider(value = WorkflowPostFunctionModuleProvider.class, products = {ProductFilter.JIRA})
    private List<WorkflowPostFunctionCapabilityBean> jiraWorkflowPostFunctions;
    
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

    /**
     * @schemaTitle User Profile Page
     */
    @CapabilityModuleProvider(value = ProfilePageModuleProvider.class, products = {ProductFilter.CONFLUENCE}) // Note: Jira uses jiraProfileTabPanels instead
    private List<ConnectPageCapabilityBean> profilePages;

    @CapabilityModuleProvider(WebHookModuleProvider.class)
    private List<WebHookCapabilityBean> webhooks;

    @ConnectModule (value = SearchRequestViewModuleProvider.class, products = {ProductFilter.JIRA})
    private List<SearchRequestViewModuleBean> jiraSearchRequestViews;

    public ModuleList()
    {
        this.jiraComponentTabPanels = newArrayList();
        this.jiraIssueTabPanels = newArrayList();
        this.jiraProjectAdminTabPanels = newArrayList();
        this.jiraProjectTabPanels = newArrayList();
        this.jiraVersionTabPanels = newArrayList();
        this.jiraProfileTabPanels = newArrayList();
        this.webItems = newArrayList();
        this.webPanels = newArrayList();
        this.generalPages = newArrayList();
        this.adminPages = newArrayList();
        this.profilePages = newArrayList();
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
        if (null == jiraProfileTabPanels)
        {
            this.jiraProfileTabPanels = newArrayList();
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
        if (null == profilePages)
        {
            this.profilePages = newArrayList();
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

    public List<ConnectTabPanelCapabilityBean> getJiraProfileTabPanels()
    {
        return jiraProfileTabPanels;
    }

    public List<WebPanelCapabilityBean> getWebPanels()
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

    public List<ConnectPageCapabilityBean> getProfilePages()
    {
        return profilePages;
    }

    public List<WebHookModuleBean> getWebhooks()
    {
        return webhooks;
    }

    public List<SearchRequestViewModuleBean> getJiraSearchRequestViews()
    {
        return jiraSearchRequestViews;
    }
}
