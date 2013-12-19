package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Page modules allow add-ons to insert new pages into atlassian products.
 * These can be automatically resized to the width and height of your add-on's content.
 * The location attribute defines where links to the new page appear.
 *
 * Each type of page displays differently:
 *
 * * `generalPages` - have no extra styling and by default a link to the page is displayed in the main navigation menu.
 * * `adminPages` - display in the administration area. Appropriate menus and other styling appear around your content.
 * * `profilePages` - ( __Confluence only__) displayed as sections inside user profiles. Like admin pages, they appear with all the necessary components around them (such as menus).
 *
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#PAGE_EXAMPLE}
 * @since 1.0
 */
public class ConnectPageModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * The url to retrieve the content from.
     * This can be absolute or relative to the addon's baseUrl
     */
    @Required
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
     *  Specifies the URL targeted by the page. The URL is relative to the add-on's base URL.
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
     */
    public Integer getWeight()
    {
        return weight;
    }

    /**
     *  An optional icon to display with the link text or as the link, specified by URL to its hosted location.
     *  You can specify a particular width and height for the icon. Most link icons in Atlassian applications
     *  are 16 by 16 pixels.
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
                .add("key", getKey())
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
                .append(super.hashCode())
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
