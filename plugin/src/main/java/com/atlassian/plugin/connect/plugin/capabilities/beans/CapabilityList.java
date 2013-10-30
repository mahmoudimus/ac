package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilityModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.ProductFilter;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.*;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean.newRemoteContainerBean;
import static com.google.common.collect.Lists.newArrayList;

/**
 * This class represents the list of modules in the json descriptor.
 * Every new module type needs to be added here as a private field and annotated with @CapabilityModuleProvider
 * 
 * Note: this class does NOT have a builder. Instead the {@link ConnectAddonBean} has a special reflective builder
 * that will handle adding beans to the proper fields in this class by name and type.
 * You can buy me a beer later for that little trick when you realize you don't need to keep updating a builder everytime you add a new type here.
 */
public class CapabilityList extends BaseCapabilityBean
{
    @CapabilityModuleProvider(WebItemModuleProvider.class)
    private List<WebItemCapabilityBean> webItems;

    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> componentTabPanels;

    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> issueTabPanels;

    @CapabilityModuleProvider(ConnectProjectAdminTabPanelModuleProvider.class)
    private List<ConnectProjectAdminTabPanelCapabilityBean> projectAdminTabPanels;

    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> projectTabPanels;

    @CapabilityModuleProvider(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelCapabilityBean> versionTabPanels;

    @CapabilityModuleProvider(WebPanelModuleProvider.class)
    private List<WebPanelCapabilityBean> webPanels;

    @CapabilityModuleProvider(value = WorkflowPostFunctionModuleProvider.class, products = {ProductFilter.JIRA})
    private List<WorkflowPostFunctionCapabilityBean> workflowPostFunctions;

    @CapabilityModuleProvider(RemoteContainerModuleProvider.class)
    private RemoteContainerCapabilityBean connectContainer;

    public CapabilityList()
    {
        this.componentTabPanels = newArrayList();
        this.connectContainer = newRemoteContainerBean().build();
        this.issueTabPanels = newArrayList();
        this.projectAdminTabPanels = newArrayList();
        this.projectTabPanels = newArrayList();
        this.versionTabPanels = newArrayList();
        this.webItems = newArrayList();
        this.webPanels = newArrayList();
        this.workflowPostFunctions = newArrayList();
    }

    public CapabilityList(BaseCapabilityBeanBuilder builder)
    {
        super(builder);

        if (null == componentTabPanels)
        {
            this.componentTabPanels = newArrayList();
        }
        if (null == connectContainer)
        {
            this.connectContainer = newRemoteContainerBean().build();
        }
        if (null == issueTabPanels)
        {
            this.issueTabPanels = newArrayList();
        }
        if (null == projectAdminTabPanels)
        {
            this.projectAdminTabPanels = newArrayList();
        }
        if (null == projectTabPanels)
        {
            this.projectTabPanels = newArrayList();
        }
        if (null == versionTabPanels)
        {
            this.versionTabPanels = newArrayList();
        }
        if (null == webItems)
        {
            this.webItems = newArrayList();
        }
        if (null == webPanels)
        {
            this.webPanels = newArrayList();
        }
        if (null == workflowPostFunctions)
        {
            this.workflowPostFunctions = newArrayList();
        }
    }

    public List<WebItemCapabilityBean> getWebItems()
    {
        return webItems;
    }

    public List<ConnectTabPanelCapabilityBean> getComponentTabPanels()
    {
        return componentTabPanels;
    }

    public List<ConnectTabPanelCapabilityBean> getIssueTabPanels()
    {
        return issueTabPanels;
    }

    public List<ConnectProjectAdminTabPanelCapabilityBean> getProjectAdminTabPanels()
    {
        return projectAdminTabPanels;
    }

    public List<ConnectTabPanelCapabilityBean> getProjectTabPanels()
    {
        return projectTabPanels;
    }

    public List<ConnectTabPanelCapabilityBean> getVersionTabPanels()
    {
        return versionTabPanels;
    }

    public List<WebPanelCapabilityBean> getWebPanels()
    {
        return webPanels;
    }

    public List<WorkflowPostFunctionCapabilityBean> getWorkflowPostFunctions()
    {
        return workflowPostFunctions;
    }

    public RemoteContainerCapabilityBean getConnectContainer()
    {
        return connectContainer;
    }
}
