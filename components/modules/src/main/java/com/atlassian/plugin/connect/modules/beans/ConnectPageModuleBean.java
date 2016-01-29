package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.*;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * <p>Page modules allow add-ons to insert new pages into atlassian products. These can be automatically resized to the width
 * and height of your add-on's content. The location attribute defines where links to the new page appear.</p>
 *
 * <p>Each type of page displays differently:
 *
 * * `generalPages` - have no extra styling and by default a link to the page is displayed in the main navigation menu.
 * * `adminPages` - display in the administration area. Appropriate menus and other styling appear around your content.
 * * `profilePages` - (__Confluence only__) displayed as sections inside user profiles.
 * * `configurePage` - used to configure the addon itself. A "Configure" button will link to this page from the addon's entry in _Manage Add-ons_.
 * * `postInstallPage` - used to provide information about the add-on after it is installed. A "Get Started" button will link to this page from the addon's entry in _Manage Add-ons_.
 *
 * <!-- ## Single page objects -->
 *
 * <p>Note that unlike other module types, an add-on may only define a single `configurePage` and a single `postInstallPage`.
 * They should each be defined in the descriptor as a single JSON object, not as a JSON array like other modules. See the examples below.</p>
 *
 * <!-- ## Seamless iframes -->
 *
 * <p>The content for a page module is injected into the Atlassian application in the form of a "seamless" iframe.
 * Seamless iframes are regular HTML iframes but with the characteristics described below.</p>
 *
 * <p>As implied here, for most page content modules, you do not need to be concerned with iframe sizing.
 * It's all handled for you. However, an exception exists for inline macros.</p>
 *
 * * Their size is based on the page height and width inside the iframe (i.e., no scrollbars)
 * * They are dynamically resized based on the inner content or relative browser window sizing
 * * They appear without borders, making them look like a non-iframed fragment of the page
 * * For general-pages, you can also opt to size your iframe to take up all of the browser window's space (instead of resizing to its internal content).
 * * To do this, add the data-option attribute "sizeToParent:true" in the script tag for all.js. It is also possible to hide footer for such pages.
 * * For example, using ACE:
 *
 *   <pre><code>
 *&lt;script src=&quot;{{hostScriptUrl}}&quot;
 *       type=&quot;text/javascript&quot;
 *       data-options=&quot;sizeToParent:true;hideFooter:true&quot;&gt;
 *&lt;/script&gt;
 *   </code></pre>
 *
 *#### Example
 *
 * @schemaTitle Page
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#PAGE_EXAMPLE}
 * @since 1.0
 */
@ObjectSchemaAttributes(maxProperties = 10000, docOverrides = {@FieldDocOverride
            (
                fieldName = "conditions",
                description = 
                        "Conditions can be added to display only when all the given conditions are true.\n\n" +
                        "The only supported conditions for pages are:\n" +
                        "<ul>\n" +
                        "<li><code>entity_property_equal_to</code>\n" +
                        "<li><code>feature_flag</code>\n" +
                        "<li><code>user_is_admin</code>\n" +
                        "<li><code>user_is_logged_in</code>\n" +
                        "<li><code>user_is_sysadmin</code>\n" +
                        "<li><code>addon_is_licensed</code>\n" +
                        "</ul>"
            )
    }
)
@SchemaDefinition("pageModule")
public class ConnectPageModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * The url to retrieve the content from.
     * This must be relative to the add-on's baseUrl. 
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    @CommonSchemaAttributes(defaultValue = "100")
    private Integer weight;

    private String location;

    private IconBean icon;

    public ConnectPageModuleBean()
    {
        init();
    }

    public ConnectPageModuleBean(ConnectPageModuleBeanBuilder builder)
    {
        super(builder);
        init();
    }

    private void init()
    {
        // Note: weight is not defaulted here. Defaulting is done later by delegating to the product accessor
        if (null == url)
        {
            this.url = "";
        }
        if (null == location)
        {
            this.location = "";
        }
    }

    /**
     * Specifies the URL targeted by the page. The URL is relative to the add-on's base URL.
     *
     * @return the URL of the page
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Determines the order in which the page's link appears in the menu or list.
     *
     * The "lightest" weight (i.e., lowest number) appears first, rising relative to other items,
     * while the "heaviest" weights sink to the bottom of the menu or list.
     *
     * Built-in web items have weights that are incremented by numbers that leave room for additional
     * items, such as by 10 or 100. Be mindful of the weight you choose for your item, so that it appears
     * in a sensible order given existing items.
     *
     * @return the weight of the page
     */
    public Integer getWeight()
    {
        return weight;
    }

    /**
     *  An optional icon to display with the link text or as the link, specified by URL to its hosted location.
     *  You can specify a particular width and height for the icon. Most link icons in Atlassian applications
     *  are 16 by 16 pixels.
     *
     * @return the icon associated with the link to the page
     */
    public IconBean getIcon()
    {
        return icon;
    }

    /**
     * The location in the application interface where the page's link should appear. For the Atlassian application
     * interface, a location is something like the coordinates on a map. It points to a particular drop-down menu or
     * navigation list in the UI.
     *
     * Places in the Atlassian UI are identified by what are known as "well-known locations."
     * For example, the "system.admin/globalsettings" location is in the administrative
     * menu link on the left side of the Administration Console.
     *
     * Product location documentation:
     *
     * <ul>
     * <li><a href="https://developer.atlassian.com/display/JIRADEV/Web+Fragments">JIRA locations</a></li>
     * <li><a href="https://developer.atlassian.com/display/CONFDEV/Web+UI+Modules">Confluence locations</a></li>
     * </ul>
     *  
     * If the <code>location</code> property is not specified, a default location is used:
     *
     * JIRA:
     *
     * <ul> 
     * <li><code>generalPage</code>: system.top.navigation.bar</li>
     * <li><code>adminPage</code>: advanced\_menu\_section/advanced_section</li>
     * </ul> 
     *
     * Confluence:
     *
     * <ul>
     * <li><code>generalPage</code>: system.browse</li>
     * <li><code>adminPage</code>: system.admin/marketplace_confluence</li>
     * <li><code>profilePage</code>: system.profile</li>
     * </ul>
     *
     * You may wish to have no link to the page shown anywhere - for example, if you are using the page as the
     * target of a <a href="../../javascript/module-Dialog.html">JavaScript API dialog</a>. 
     * In this case, set the value of <code>location</code> to "none".
     *
     * @return the location of the link to the page
     */ 
    public String getLocation()
    {
        return location;
    }

    @Override
    public String toString()
    {
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper(this);
        appendToStringFields(toStringHelper);
        return toStringHelper.toString();
    }

    protected void appendToStringFields(Objects.ToStringHelper toStringHelper)
    {
        toStringHelper
                .add("name", getName())
                .add("key", getRawKey())
                .add("url", getUrl())
                .add("weight", getWeight())
                .add("icon", getIcon())
                .add("location", getLocation());
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ConnectPageModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        ConnectPageModuleBean other = (ConnectPageModuleBean) otherObj;

        return new EqualsBuilder()
                .append(url, other.url)
                .append(weight, other.weight)
                .append(icon, other.icon)
                .append(location, other.location)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(11, 23)
                .appendSuper(super.hashCode())
                .append(url)
                .append(weight)
                .append(icon)
                .append(location)
                .build();
    }


    public static ConnectPageModuleBeanBuilder newPageBean()
    {
        return new ConnectPageModuleBeanBuilder();
    }

    public static ConnectPageModuleBeanBuilder newPageBean(ConnectPageModuleBean defaultBean)
    {
        return new ConnectPageModuleBeanBuilder(defaultBean);
    }
}
