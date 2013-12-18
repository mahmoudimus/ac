package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
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
 * that will handle adding beans to the proper fields in this class by name and type. You can buy me a beer later for
 * that little trick when you realize you don't need to keep updating a builder every time you add a new type here.
 *
 * Below is the public documentation
 *
 *            ;;;;;
 *            ;;;;;
 *            ;;;;;
 *            ;;;;;
 *          ..;;;;;..
 *           ':::::'
 *             ':`
 */

/**
 * Modules are UI extension points that add-ons can use to insert content into various areas of the host application's
 * interface. You implement a page module (along with others type of module you can use with Atlassian Connect, like
 * webhooks) by declaring it in the add-on descriptor and implementing the add-on code that composes it.
 *
 * Each application has module types that are specific for it, but there are some common types as well. For instance,
 * both JIRA and Confluence support the `generalPages` module, but only Confluence has `profilePage`. An add-on can
 * implement as many modules as needed. For example, a typical add-on would likely provide modules for at least one
 * lifecycle element, a configuration page, and possibly multiple general pages. All modules declarations must have a
 * `url` attribute. The url attribute identifies the path on the add-on host to the resource that implements the module.
 * The URL value must be valid relative to the `baseUrl` value in the add-on descriptor.
 */
@SuppressWarnings ("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
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

    /**
     * @schemaTitle User Profile Tab Panel
     */
    @ConnectModule(value = ConnectTabPanelModuleProvider.class, products = {ProductFilter.JIRA})
    private List<ConnectTabPanelModuleBean> jiraProfileTabPanels;

    @ConnectModule(value = WorkflowPostFunctionModuleProvider.class, products = {ProductFilter.JIRA})
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

    /**
     * A configure page module is a page module used to configure the addon itself.
     * Other than that it is the same as other pages.
     *
     * @exampleJson {@see ConnectJsonExamples#CONFIGURE_PAGE_EXAMPLE}
=    * @schemaTitle Configure Page
     */
    @ConnectModule (ConfigurePageModuleProvider.class)
    private ConnectPageModuleBean configurePage;

    /**
     * @schemaTitle User Profile Page
     */
    @ConnectModule(value = ProfilePageModuleProvider.class, products = {ProductFilter.CONFLUENCE}) // Note: Jira uses jiraProfileTabPanels instead
    private List<ConnectPageModuleBean> profilePages;

    @ConnectModule(WebHookModuleProvider.class)
    private List<WebHookModuleBean> webhooks;

    @ConnectModule (value = SearchRequestViewModuleProvider.class, products = {ProductFilter.JIRA})
    private List<SearchRequestViewModuleBean> jiraSearchRequestViews;

    @ConnectModule (value = DynamicContentMacroModuleProvider.class, products = {ProductFilter.CONFLUENCE})
    private List<DynamicContentMacroModuleBean> dynamicContentMacros;

    @ConnectModule (value = StaticContentMacroModuleProvider.class, products = {ProductFilter.CONFLUENCE})
    private List<StaticContentMacroModuleBean> staticContentMacros;

    public ModuleList()
    {
        this.adminPages = newArrayList();
        this.dynamicContentMacros = newArrayList();
        this.generalPages = newArrayList();
        this.jiraComponentTabPanels = newArrayList();
        this.jiraIssueTabPanels = newArrayList();
        this.jiraProfileTabPanels = newArrayList();
        this.jiraProjectAdminTabPanels = newArrayList();
        this.jiraProjectTabPanels = newArrayList();
        this.jiraSearchRequestViews = newArrayList();
        this.jiraVersionTabPanels = newArrayList();
        this.jiraWorkflowPostFunctions = newArrayList();
        this.profilePages = newArrayList();
        this.staticContentMacros = newArrayList();
        this.webhooks = newArrayList();
        this.webItems = newArrayList();
        this.webPanels = newArrayList();
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
        if (null == dynamicContentMacros)
        {
            this.dynamicContentMacros = newArrayList();
        }
        if (null == staticContentMacros)
        {
            this.staticContentMacros = newArrayList();
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

    public List<ConnectTabPanelModuleBean> getJiraProfileTabPanels()
    {
        return jiraProfileTabPanels;
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

    public ConnectPageModuleBean getConfigurePage()
    {
        return configurePage;
    }

    public List<ConnectPageModuleBean> getProfilePages()
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

    public List<DynamicContentMacroModuleBean> getDynamicContentMacros()
    {
        return dynamicContentMacros;
    }

    public List<StaticContentMacroModuleBean> getStaticContentMacros()
    {
        return staticContentMacros;
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
                .append(dynamicContentMacros, other.dynamicContentMacros)
                .append(configurePage, other.configurePage)
                .append(generalPages, other.generalPages)
                .append(jiraComponentTabPanels, other.jiraComponentTabPanels)
                .append(jiraIssueTabPanels, other.jiraIssueTabPanels)
                .append(jiraProfileTabPanels, other.jiraProfileTabPanels)
                .append(jiraProjectAdminTabPanels, other.jiraProjectAdminTabPanels)
                .append(jiraProjectTabPanels, other.jiraProjectTabPanels)
                .append(jiraSearchRequestViews, other.jiraSearchRequestViews)
                .append(jiraVersionTabPanels, other.jiraVersionTabPanels)
                .append(jiraWorkflowPostFunctions, other.jiraWorkflowPostFunctions)
                .append(profilePages, other.profilePages)
                .append(staticContentMacros, other.staticContentMacros)
                .append(webhooks, other.webhooks)
                .append(webItems, other.webItems)
                .append(webPanels, webPanels)
                .build();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(29, 37)
                .append(adminPages)
                .append(dynamicContentMacros)
                .append(configurePage)
                .append(generalPages)
                .append(jiraComponentTabPanels)
                .append(jiraIssueTabPanels)
                .append(jiraProfileTabPanels)
                .append(jiraProjectAdminTabPanels)
                .append(jiraProjectTabPanels)
                .append(jiraSearchRequestViews)
                .append(jiraVersionTabPanels)
                .append(jiraWorkflowPostFunctions)
                .append(profilePages)
                .append(staticContentMacros)
                .append(webhooks)
                .append(webItems)
                .append(webPanels)
                .build();
    }
}
