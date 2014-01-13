package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;

/**
 * Adds a panel (or section) to a page in the Atlassian application. Panels let you present related information and
 * controls in the application interface as a group. For example, the existing "People" panel in JIRA issue view
 * shows the assignee, reporter, and similar information for the issue.
 * <p/>
 * #### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBPANEL_EXAMPLE}
 * @schemaTitle Web Panel
 * @since 1.0
 */
public class WebPanelModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * Location in the application interface for this panel.
     */
    private String location;
    /**
     * The width and height of the web panel on the page.
     */
    private WebPanelLayout layout;
    /**
     * The URL of the add-on resource that provides the web panel content.
     */
    private String url;
    /**
     * Determines the order in which web panels appear. Web panels are displayed top to bottom or left to right in
     * order of ascending weight. The "lightest" weight is displayed first, while the "heaviest" weights sink to the
     * bottom. The weight values for most panels start from 100, and the weights for the links
     * generally start from 10. The weight is incremented by 10 for each in sequence to leave room for custom
     * panels.
     */
    private Integer weight;

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

}
