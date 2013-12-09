package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilityModuleProvider;
import com.atlassian.plugin.spring.scanner.ProductFilter;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.*;

import static com.google.common.collect.Lists.newArrayList;

/**
 * This class represents the list of modules in the json descriptor.
 * Every new module type needs to be added here as a private field and annotated with @CapabilityModuleProvider
 * 
 * The field name will be what appears in the json.
 * 
 * Note: this class does NOT have a builder. Instead the {@link ConnectAddonBean} has a special reflective builder
 * that will handle adding beans to the proper fields in this class by name and type.
 * You can buy me a beer later for that little trick when you realize you don't need to keep updating a builder everytime you add a new type here.
 */
public class CapabilityList extends BaseCapabilityBean
{
    @CapabilityModuleProvider(WebItemModuleProvider.class)
    private List<WebItemCapabilityBean> webItems;

    /**
     * @schemaTitle Component Tab Panel
     */
    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraComponentTabPanels;

    /**
     * @schemaTitle Issue Tab Panel
     */
    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraIssueTabPanels;

    @CapabilityModuleProvider(value = ConnectProjectAdminTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectProjectAdminTabPanelCapabilityBean> jiraProjectAdminTabPanels;

    /**
     * @schemaTitle Project Tab Panel
     */
    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraProjectTabPanels;

    /**
     * @schemaTitle Version Tab Panel
     */
    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraVersionTabPanels;

    /**
     * @schemaTitle User Profile Tab Panel
     */
    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraProfileTabPanels;

    @CapabilityModuleProvider(value = WorkflowPostFunctionModuleProvider.class, products = {ProductFilter.JIRA})
    private List<WorkflowPostFunctionCapabilityBean> jiraWorkflowPostFunctions;
    
    @CapabilityModuleProvider(WebPanelModuleProvider.class)
    private List<WebPanelCapabilityBean> webPanels;

    /**
     * @schemaTitle General Page
     */
    @CapabilityModuleProvider(GeneralPageModuleProvider.class)
    private List<ConnectPageCapabilityBean> generalPages;

    /**
     * @schemaTitle Admin Page
     */
    @CapabilityModuleProvider(AdminPageModuleProvider.class)
    private List<ConnectPageCapabilityBean> adminPages;

    /**
     * @schemaTitle User Profile Page
     */
    @CapabilityModuleProvider(value = ProfilePageModuleProvider.class, products = {ProductFilter.CONFLUENCE}) // Note: Jira uses jiraProfileTabPanels instead
    private List<ConnectPageCapabilityBean> profilePages;

    @CapabilityModuleProvider(WebHookModuleProvider.class)
    private List<WebHookCapabilityBean> webhooks;

    @CapabilityModuleProvider(value = SearchRequestViewModuleProvider.class, products = {ProductFilter.JIRA})
    private List<SearchRequestViewCapabilityBean> jiraSearchRequestViews;

    public CapabilityList()
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

    public CapabilityList(BaseCapabilityBeanBuilder builder)
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

    public List<WebItemCapabilityBean> getWebItems()
    {
        return webItems;
    }

    public List<ConnectTabPanelCapabilityBean> getJiraComponentTabPanels()
    {
        return jiraComponentTabPanels;
    }

    public List<ConnectTabPanelCapabilityBean> getJiraIssueTabPanels()
    {
        return jiraIssueTabPanels;
    }

    public List<ConnectProjectAdminTabPanelCapabilityBean> getJiraProjectAdminTabPanels()
    {
        return jiraProjectAdminTabPanels;
    }

    public List<ConnectTabPanelCapabilityBean> getJiraProjectTabPanels()
    {
        return jiraProjectTabPanels;
    }

    public List<ConnectTabPanelCapabilityBean> getJiraVersionTabPanels()
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

    public List<WorkflowPostFunctionCapabilityBean> getJiraWorkflowPostFunctions()
    {
        return jiraWorkflowPostFunctions;
    }

    public List<ConnectPageCapabilityBean> getGeneralPages()
    {
        return generalPages;
    }

    public List<ConnectPageCapabilityBean> getAdminPages()
    {
        return adminPages;
    }

    public List<ConnectPageCapabilityBean> getProfilePages()
    {
        return profilePages;
    }

    public List<WebHookCapabilityBean> getWebhooks()
    {
        return webhooks;
    }

    public List<SearchRequestViewCapabilityBean> getJiraSearchRequestViews()
    {
        return jiraSearchRequestViews;
    }
}
