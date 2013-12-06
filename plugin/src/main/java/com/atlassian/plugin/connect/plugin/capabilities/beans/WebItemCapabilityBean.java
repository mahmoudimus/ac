package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebItemCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;

/**
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
 * @schemaTitle Web Item
 * @since 1.0
 */
public class WebItemCapabilityBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     *  Specifies the URL targeted by the link. The URL can be absolute or relative to either the
     *  product URL or the add-on's base URL, depending on the {@link AddOnUrlContext} parameter.
     */
    private String link;

    /**
     * The location in the application interface where the web item should appear. For the Atlassian application
     * interface, a location is something like the coordinates on a map. It points to a particular drop-down menu or
     * navigation list in the UI.
     *
     * <p/>
     *
     * Places in the Atlassian UI are identified by what are known as "well-known locations."
     * For example, the "system.admin/globalsettings" location is in the administrative
     * menu link on the left side of the Administration Console.
     */
    private String location;

    /**
     *  The context for the URL parameter, if the URL is specified as a relative (not absolute) URL.
     *
     * <p/>
     *
     *  This context can be either "add-on", which renders the URL relative to the add-on's base URL, or
     *  "product", which renders the URL relative to the product's base URL.
     */
    private AddOnUrlContext context;

    /**
     * Determines the order in which the web item appears in the menu or list.
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
    private Integer weight;

    /**
     *  Defines the way the link is opened in the browser, such as in a modal or inline dialog.
     *  If omitted, the link behaves as a regular hyperlink.
     */
    private WebItemTargetBean target;

    /**
     * Specifies custom styles for the web link target page, if desired. By default, the web item content gets
     * styled in the default style of the target application.
     */
    private List<String> styleClasses;

    /**
     * The internationalised text to be used in the link's tooltip.
     */
    private I18nProperty tooltip;

    /**
     *  An optional icon to display with the link text or as the link, specified by URL to its hosted location.
     *  You can specify a particular width and height for the icon. Most link icons in Atlassian applications
     *  are 16 by 16 pixels.
     */
    private IconBean icon;

    public WebItemCapabilityBean()
    {
        this.link = "";
        this.location = "";
        this.context = AddOnUrlContext.addon;
        this.weight = 100;
        this.target = newWebItemTargetBean().build();
        this.styleClasses = new ArrayList<String>();
        this.tooltip = new I18nProperty("", "");
        this.icon = newIconBean().withWidth(0).withHeight(0).withUrl("").build();
    }

    public WebItemCapabilityBean(WebItemCapabilityBeanBuilder builder)
    {
        super(builder);
        
        if (null == link)
        {
            this.link = "";
        }
        
        if(null == context)
        {
            this.context = AddOnUrlContext.addon;
        }
        
        if(null == weight)
        {
            this.weight = 100;
        }
        
        if(null == target)
        {
            this.target = newWebItemTargetBean().build();
        }
        
        if (null == location)
        {
            this.location = "";
        }
        if (null == styleClasses)
        {
            this.styleClasses = new ArrayList<String>();
        }
        if (null == tooltip)
        {
            this.tooltip = new I18nProperty("", "");
        }
        if (null == icon)
        {
            this.icon = newIconBean().withWidth(16).withHeight(16).withUrl("").build();
        }
    }

    public String getLink()
    {
        return link;
    }

    public String getLocation()
    {
        return location;
    }

    public AddOnUrlContext getContext()
    {
        return context;
    }

    public int getWeight()
    {
        return weight;
    }
    
    public WebItemTargetBean getTarget()
    {
        return target;
    }

    public List<String> getStyleClasses()
    {
        return styleClasses;
    }

    public I18nProperty getTooltip()
    {
        return tooltip;
    }

    public IconBean getIcon()
    {
        return icon;
    }

    public boolean isAbsolute()
    {
        return (null != getLink() && getLink().toLowerCase().startsWith("http"));
    }
    
    public static WebItemCapabilityBeanBuilder newWebItemBean()
    {
        return new WebItemCapabilityBeanBuilder();
    }

    public static WebItemCapabilityBeanBuilder newWebItemBean(WebItemCapabilityBean defaultBean)
    {
        return new WebItemCapabilityBeanBuilder(defaultBean);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("key", getKey())
                .add("name", getName())
                .add("link", getLink())
                .add("location", getLocation())
                .add("weight", getWeight())
                .add("styleClasses", getStyleClasses())
                .add("tooltip", getTooltip())
                .add("target", getTarget())
                .toString();
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof WebItemCapabilityBean && super.equals(otherObj)))
        {
            return false;
        }

        WebItemCapabilityBean other = (WebItemCapabilityBean) otherObj;

        return new EqualsBuilder()
                .append(link, other.link)
                .append(location, other.location)
                .append(context, other.context)
                .append(weight, other.weight)
                .append(target, other.target)
                .append(styleClasses, other.styleClasses)
                .append(tooltip, other.tooltip)
                .append(icon, other.icon)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 61)
                .append(link)
                .append(location)
                .append(context)
                .append(weight)
                .append(target)
                .append(styleClasses)
                .append(tooltip)
                .append(icon)
                .build();
    }
}
