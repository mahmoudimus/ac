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
import java.util.Map;

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
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.GeneralPageModuleProvider")
    private List<ConnectPageModuleBean> generalPages;

    /**
     * An admin page module is used to provide an administration chrome for add-on content.
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.AdminPageModuleProvider")
    private List<ConnectPageModuleBean> adminPages;

    /**
     * A configure page module is a page module used to configure the addon itself.
     * It's link will appear in the add-ons entry in 'Manage Add-ons'.
     */
    @ConnectModule("com.atlassian.plugin.connect.plugin.capabilities.provider.ConfigurePageModuleProvider")
    private ConnectPageModuleBean configurePage;



    public ModuleList()
    {
        this.adminPages = newArrayList();
        this.generalPages = newArrayList();
        this.webhooks = newArrayList();
        this.webItems = newArrayList();
        this.webPanels = newArrayList();
        this.webSections = newArrayList();
    }

    public ModuleList(BaseModuleBeanBuilder builder)
    {
        super(builder);

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
        if (null == webhooks)
        {
            this.webhooks = newArrayList();
        }
    }

    public List<WebItemModuleBean> getWebItems()
    {
        return webItems;
    }

    public List<WebPanelModuleBean> getWebPanels()
    {
        return webPanels;
    }

    public List<WebSectionModuleBean> getWebSections()
    {
        return webSections;
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

    public List<WebHookModuleBean> getWebhooks()
    {
        return webhooks;
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
                .append(configurePage, other.configurePage)
                .append(generalPages, other.generalPages)
                .append(webhooks, other.webhooks)
                .append(webItems, other.webItems)
                .append(webPanels, other.webPanels)
                .append(webSections, other.webSections)
                .build();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(29, 37)
                .append(adminPages)
                .append(configurePage)
                .append(generalPages)
                .append(webhooks)
                .append(webItems)
                .append(webPanels)
                .append(webSections)
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
