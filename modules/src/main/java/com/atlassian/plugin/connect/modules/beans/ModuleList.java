package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedListWithType;
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
 * both JIRA and Confluence support the `generalPages` module, but only Confluence has `profilePage`.
 *
 * An add-on can implement as many modules as needed. For example, a typical add-on would likely provide modules for at
 * least one lifecycle element, a configuration page, and possibly multiple general pages.
 *
 * Here's an example of a module declaration:
 *
 *    {
 *        "name": "My Addon",
 *        "modules": {
 *            "webItems": [{
 *                "conditions": [
 *                    {
 *                        "condition": "sub_tasks_enabled"
 *                    },
 *                    {
 *                        "condition": "is_issue_editable"
 *                    },
 *                    {
 *                        "condition": "is_issue_unresolved"
 *                    }
 *                ],
 *                "location": "operations-subtasks",
 *                "url": "/dialog",
 *                "name": {
 *                    "value": "Create Sub-Tasks"
 *                },
 *                "target": {
 *                    "type": "dialog"
 *                }
 *            }]
 *        }
 *    }
 *
 * In this case, we're declaring a web item which opens as a dialog. This declaration adds a dialog box to JIRA that
 * users can open by clicking a "Create Sub-Tasks" link on an issue.
 *
 *### Conditions
 *
 * You can specify the conditions in which the link (and therefore access to this page) appears. The Atlassian application
 * ensures that the link only appears if it is appropriate for it to do so. In the example, the module should only appear
 * if subtasks are enabled and the issue is both editable and unresolved.. The condition elements state conditions that
 * must be true for the module to be in effect. Note, the condition only applies to the presence or absence of the link.
 * You should still permission the URL that the link references if appropriate.
 *
 *### URLs
 *
 * All module declarations must have a `url` attribute. The url attribute identifies the path on the add-on host to the
 * resource that implements the module. The URL value must be valid relative to the `baseUrl` value in the add-on descriptor.
 *
 * The url value in our example is `/dialog`. This must be a resource that is accessible on your server (relative to the
 * base URL of the add-on). It presents the content that appears in the iframe dialog; in other words, the HTML,
 * JavaScript, or other type of web content source that composes the iframe content.
 *
 * Note: for a webhook, the URL should be the address to which the Atlassian application posts notifications. For other
 * modules, such as `generalPages` or `webItems`, the URL identifies the web content to be used to compose the page.
 *
 * You can request certain pieces of contextual data, such as a project or space key, to be included in the URLs
 * requested from your add-on. See passing [Context Parameters](../../concepts/context-parameters.html).
 */
@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class ModuleList extends BaseModuleBean
{
    /////////////////////////////////////////////////////
    ///////    COMMON MODULES
    /////////////////////////////////////////////////////

    /**
     * The Web Item module allows you to define new links in application menus.
     *
     * @schemaTitle Web Item
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultWebItemModuleProvider")
    private List<WebItemModuleBean> webItems;

    /**
     * The Web Panel module allows you to define panels, or sections, on an HTML page.
     * A panel is an iFrame that will be inserted into a page.
     *
     * @schemaTitle Web Panel
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.WebPanelModuleProvider")
    private List<WebPanelModuleBean> webPanels;

    /**
     * The Web Section plugin module allows you to define new sections in application menus.
     *
     * @schemaTitle Web Section
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.WebSectionModuleProvider")
    private List<WebSectionModuleBean> webSections;

    /**
     * The Web Hook module allows you be notified of key events that occur in the host product
     *
     * @schemaTitle Webhook
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.WebHookModuleProvider")
    private List<WebHookModuleBean> webhooks;

    /**
     * A general page module is used to provide a generic chrome for add-on content in the product.
     *
     * @schemaTitle General Page
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.GeneralPageModuleProvider")
    private List<ConnectPageModuleBean> generalPages;

    /**
     * An admin page module is used to provide an administration chrome for add-on content.
     *
     * @schemaTitle Admin Page
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.AdminPageModuleProvider")
    private List<ConnectPageModuleBean> adminPages;

    /**
     * A configure page module is a page module used to configure the addon itself.
     * It's link will appear in the add-ons entry in 'Manage Add-ons'.
     *
     * @schemaTitle Configure Page
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.ConfigurePageModuleProvider")
    private ConnectPageModuleBean configurePage;


    /////////////////////////////////////////////////////
    ///////    JIRA MODULES
    /////////////////////////////////////////////////////

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
     * The Report module allows you to add new reports in project page.
     * @schemaTitle Report
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ReportModuleProvider", products = {ProductFilter.JIRA})
    private List<ReportModuleBean> jiraReports;

    /////////////////////////////////////////////////////
    ///////    CONFLUENCE MODULES
    /////////////////////////////////////////////////////

    /**
     * Dynamic content macros allow you to add a macro into a Confluence page which is rendered as an iframe.
     *
     * @schemaTitle Dynamic Content Macro
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.DynamicContentMacroModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<DynamicContentMacroModuleBean> dynamicContentMacros;

    /**
     * @schemaTitle User Profile Page
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.ProfilePageModuleProvider", products = {ProductFilter.CONFLUENCE}) // Note: Jira uses jiraProfileTabPanels instead
    private List<ConnectPageModuleBean> profilePages;

    /**
     * The Space Tools Tab module allows you to add new tabs to the Space Tools area of Confluence.
     * @schemaTitle Space Tools Tab
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.SpaceToolsTabModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<SpaceToolsTabModuleBean> spaceToolsTabs;

    /**
     * Static content macros allow you to add a macro into a Confluence page which is stored with the Confluence page
     * itself. The add-on is responsible for generating the rendered XHTML in
     * [Confluence Storage Format](https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format)
     *
     * @schemaTitle Static Content Macro
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.StaticContentMacroModuleProvider", products = {ProductFilter.CONFLUENCE})
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
        this.jiraEntityProperties = newArrayList();
        this.jiraReports = newArrayList();
        this.profilePages = newArrayList();
        this.spaceToolsTabs = newArrayList();
        this.staticContentMacros = newArrayList();
        this.webhooks = newArrayList();
        this.webItems = newArrayList();
        this.webPanels = newArrayList();
        this.webSections = newArrayList();
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
        if (null == jiraEntityProperties)
        {
            this.jiraEntityProperties = newArrayList();
        }
        if (null == webItems)
        {
            this.webItems = newArrayList();
        }
        if (null == webPanels)
        {
            this.webPanels = newArrayList();
        }
        if (null == webSections)
        {
            this.webSections = newArrayList();
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
        if (null == spaceToolsTabs)
        {
            this.spaceToolsTabs = newArrayList();
        }
        if (null == staticContentMacros)
        {
            this.staticContentMacros = newArrayList();
        }
        if (null == jiraReports)
        {
            this.jiraReports = newArrayList();
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

    public List<WebSectionModuleBean> getWebSections()
    {
        return webSections;
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

    public List<SpaceToolsTabModuleBean> getSpaceToolsTabs() {
        return spaceToolsTabs;
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
                .append(jiraEntityProperties, other.jiraEntityProperties)
                .append(profilePages, other.profilePages)
                .append(spaceToolsTabs, other.spaceToolsTabs)
                .append(staticContentMacros, other.staticContentMacros)
                .append(webhooks, other.webhooks)
                .append(webItems, other.webItems)
                .append(webPanels, webPanels)
                .append(webSections, webSections)
                .append(jiraReports, other.jiraReports)
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
                .append(jiraEntityProperties)
                .append(profilePages)
                .append(spaceToolsTabs)
                .append(staticContentMacros)
                .append(webhooks)
                .append(webItems)
                .append(webPanels)
                .append(webSections)
                .append(jiraReports)
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
                        beanList = moduleBean == null ? ImmutableList.<ModuleBean>of() : newArrayList(moduleBean);
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
