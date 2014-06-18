package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Adds a panel (or section) to a page in the Atlassian application. Panels let you present related information and
 * controls in the application interface as a group. For example, the existing "People" panel in JIRA issue view shows
 * the assignee, reporter, and similar information for the issue.
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBPANEL_EXAMPLE}
 * @schemaTitle Web Panel
 * @since 1.0
 */
@SchemaDefinition("webPanel")
public class WebPanelModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * The URL of the add-on resource that provides the web panel content.
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    /**
     * Location in the application interface for this panel.
     * <p/>
     * Product location documentation:
     *
     * * [JIRA locations](https://developer.atlassian.com/display/JIRADEV/Web+Fragments)
     * * [Confluence locations](https://developer.atlassian.com/display/CONFDEV/Web+UI+Modules)
     */
    @Required
    private String location;

    /**
     * The width and height of the web panel on the page.
     */
    private WebPanelLayout layout;

    /**
     * Determines the order in which web panels appear. Web panels are displayed top to bottom or left to right in order
     * of ascending weight. The "lightest" weight is displayed first, while the "heaviest" weights sink to the bottom.
     * The weight values for most panels start from 100, and the weights for the links generally start from 10. The
     * weight is incremented by 10 for each in sequence to leave room for custom panels.
     */
    private Integer weight;

    /**
     * Information about the web panel that will be shown when hovering over its header
     */
    private I18nProperty tooltip;

    public WebPanelModuleBean()
    {
        this.location = "";
        this.layout = new WebPanelLayout();
        this.url = "";
        this.weight = ConnectAddonBean.DEFAULT_WEIGHT;
    }

    public WebPanelModuleBean(WebPanelModuleBeanBuilder builder)
    {
        super(builder);

        if (null == location)
        {
            this.location = "";
        }

        if (null == layout)
        {
            this.layout = new WebPanelLayout();
        }

        if (null == url)
        {
            this.url = "";
        }

        if (null == weight)
        {
            this.weight = ConnectAddonBean.DEFAULT_WEIGHT;
        }

    }

    public String getLocation()
    {
        return location;
    }

    public WebPanelLayout getLayout()
    {
        return layout;
    }

    public String getUrl()
    {
        return url;
    }

    public Integer getWeight()
    {
        return weight;
    }

    public I18nProperty getTooltip()
    {
        return tooltip;
    }

    public boolean isAbsolute()
    {
        return (null != getUrl() && getUrl().toLowerCase().startsWith("http"));
    }

    public static WebPanelModuleBeanBuilder newWebPanelBean()
    {
        return new WebPanelModuleBeanBuilder();
    }

    public static WebPanelModuleBeanBuilder newWebPanelBean(WebPanelModuleBean defaultBean)
    {
        return new WebPanelModuleBeanBuilder(defaultBean);
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof WebPanelModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        WebPanelModuleBean other = (WebPanelModuleBean) otherObj;

        return new EqualsBuilder()
                .append(url, other.url)
                .append(location, other.location)
                .append(layout, other.layout)
                .append(weight, other.weight)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 61)
                .appendSuper(super.hashCode())
                .append(url)
                .append(location)
                .append(layout)
                .append(weight)
                .build();
    }

}
