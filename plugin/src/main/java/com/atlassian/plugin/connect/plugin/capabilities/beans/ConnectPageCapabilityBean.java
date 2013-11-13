package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectPageCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;

/**
 * Page modules are UI extension points that add-ons can use to insert content into various areas of the host
 * application's interface. You implement a page module (along with the other type of module you can use with
 * Atlassian Connect, webhooks) by declaring it in the add-on descriptor and implementing the add-on code that
 * composes it.
 *
 * Each application has page module types that are specific for it, but there are some common page types as well.
 * For instance, both JIRA and Confluence support the general-page and profile-page module, but only JIRA has the
 * issue-panel-page.
 *
 * The page module takes care of integrating the add-on content into the application for you. The add-on content
 * automatically gets the page styles and decorators from the host application.
 *
 *
 *
 * Adds a web item to a specified location in the application interface. A web item is a hyperlink
 * that’s inserted into some standard place in the Atlassian application interface, such as the
 * administration menu.
 *
 * <p/>
 *
 * The form that the link takes can vary depending on the location. For instance, a web item in the header bar
 * (with a location section of system.top.navigation.bar) adds a link to the navigation bar across the top of the
 * interface. On the other hand, a web item in the opsbar-operation location section in JIRA adds an item to the issue
 * operation buttons.
 *
 * <p/>
 *
 * A web item link can open a new page in the application or a dialog, depending on your configuration.
 *
 * <p/>
 *
 * Web links are a simple and useful way to extend Atlassian applications. If you want to extend an Atlassian
 * application and don't know where to start, a web item may be all you need.
 *
 * @since 1.0
 */
public class ConnectPageCapabilityBean extends BeanWithKeyAndParamsAndConditions
{
    private String url;
    private Integer weight;
    private String location;
    private IconBean icon;
//    private String application; TODO: Figure out what to do with application. Only reference to it I see is in DescriptorPermissionsReader. Not sure how that ties in

    public ConnectPageCapabilityBean(ConnectPageCapabilityBeanBuilder builder)
    {
        super(builder);

        // Note: weight is not defaulted here. Defaulting is done later by delegating to the product accessor
        if (null == url)
        {
            this.url = "";
        }
        if (null == location)
        {
            this.location = "";
        }
        if (null == icon)
        {
            this.icon = IconBean.newIconBean().withWidth(16).withHeight(16).withUrl("").build();
        }
    }

    /**
     *  Specifies the URL targeted by the page. The URL is relative to either the
     *  the add-on's base URL
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Determines the order in which the page's link appears in the menu or list.
     *
     * <p/>
     *
     * The "lightest" weight (i.e., lowest number) appears first, rising relative to other items,
     * while the "heaviest" weights sink to the bottom of the menu or list.
     *
     * <p/>
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
     * <p/>
     *
     * Places in the Atlassian UI are identified by what are known as "well-known locations."
     * For example, the "system.admin/globalsettings" location is in the administrative
     * menu link on the left side of the Administration Console.
     */
    public String getLocation()
    {
        return location;
    }

    public static ConnectPageCapabilityBeanBuilder newPageBean()
    {
        return new ConnectPageCapabilityBeanBuilder();
    }

    public static ConnectPageCapabilityBeanBuilder newPageBean(ConnectPageCapabilityBean defaultBean)
    {
        return new ConnectPageCapabilityBeanBuilder(defaultBean);
    }
}
