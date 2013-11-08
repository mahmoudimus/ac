package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilityModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.*;
import com.atlassian.plugin.spring.scanner.ProductFilter;

import static com.google.common.collect.Lists.newArrayList;

/**
 * This class represents the list of modules in the json descriptor.
 * Every new module type needs to be added here as a private field and annotated with @CapabilityModuleProvider
 * <p/>
 * The field name will be what appears in the json.
 * <p/>
 * Note: this class does NOT have a builder. Instead the {@link ConnectAddonBean} has a special reflective builder
 * that will handle adding beans to the proper fields in this class by name and type.
 * You can buy me a beer later for that little trick when you realize you don't need to keep updating a builder everytime you add a new type here.
 */
public class CapabilityList extends BaseCapabilityBean
{
    @CapabilityModuleProvider(WebItemModuleProvider.class)
    private List<WebItemCapabilityBean> webItems;

    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraComponentTabPanels;

    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraIssueTabPanels;

    @CapabilityModuleProvider(value = ConnectProjectAdminTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectProjectAdminTabPanelCapabilityBean> jiraProjectAdminTabPanels;

    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraProjectTabPanels;

    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> jiraVersionTabPanels;

    @CapabilityModuleProvider(value = WorkflowPostFunctionModuleProvider.class, products = {ProductFilter.JIRA})
    private List<WorkflowPostFunctionCapabilityBean> jiraWorkflowPostFunctions;

    @CapabilityModuleProvider(WebPanelModuleProvider.class)
    private List<WebPanelCapabilityBean> webPanels;

    public CapabilityList()
    {
        this.jiraComponentTabPanels = newArrayList();
        this.jiraIssueTabPanels = newArrayList();
        this.jiraProjectAdminTabPanels = newArrayList();
        this.jiraProjectTabPanels = newArrayList();
        this.jiraVersionTabPanels = newArrayList();
        this.webItems = newArrayList();
        this.webPanels = newArrayList();
        this.jiraWorkflowPostFunctions = newArrayList();
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
        if (null == webItems)
        {
            this.webItems = newArrayList();
        }
        if (null == webPanels)
        {
            this.webPanels = newArrayList();
        }
        if (null == jiraWorkflowPostFunctions)
        {
            this.jiraWorkflowPostFunctions = newArrayList();
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

    public List<WebPanelCapabilityBean> getWebPanels()
    {
        return webPanels;
    }

    public List<WorkflowPostFunctionCapabilityBean> getJiraWorkflowPostFunctions()
    {
        return jiraWorkflowPostFunctions;
    }
}
