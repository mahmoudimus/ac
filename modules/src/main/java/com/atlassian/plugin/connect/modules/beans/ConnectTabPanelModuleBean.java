package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectTabPanelModuleBeanBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tab panel modules allow add-ons to insert new elements into the following tabbed sections of the host application's
 * user interface.
 *
 * * Issue page _Activity_ section
 * * Project page sidebar
 * * User profile page sidebar
 *
 *The tab panel module takes care of integrating the add-on content into the application for you. The add-on content
 * automatically gets the tab panel styles and decorators from the host application.
 *
 *#### Example
 *
 * @schemaTitle Tab Panel
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#TAB_PANEL_EXAMPLE}
 * @since 1.0
 */
@SchemaDefinition("tabPanel")
public class ConnectTabPanelModuleBean extends BeanWithKeyAndParamsAndConditions
{
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    private Integer weight;

    public ConnectTabPanelModuleBean()
    {
        this("", 100);
    }

    public ConnectTabPanelModuleBean(String url, Integer weight)
    {
        this.url = checkNotNull(url);
        this.weight = checkNotNull(weight);
    }

    public ConnectTabPanelModuleBean(ConnectTabPanelModuleBeanBuilder builder)
    {
        super(builder);

        if (null == weight)
        {
            this.weight = 100;
        }
        if (null == url)
        {
            this.url = "";
        }
    }

    /**
     * Specifies the URL targeted by the tab panel. The URL is relative to the add-on's base URL.
     *
     * @return the URL of the tab panel
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * <p>Determines the order in which the tab panel's link appears in the menu or list.</p>
     *
     * <p>The "lightest" weight (i.e., lowest number) appears first, rising relative to other items,
     * while the "heaviest" weights sink to the bottom of the menu or list.</p>
     *
     * <p>Built-in web items have weights that are incremented by numbers that leave room for additional
     * items, such as by 10 or 100. Be mindful of the weight you choose for your item, so that it appears
     * in a sensible order given existing items.</p>
     *
     * @return the weight of the tab panel
     */
    public int getWeight()
    {
        return weight;
    }

    public static ConnectTabPanelModuleBeanBuilder newTabPanelBean()
    {
        return new ConnectTabPanelModuleBeanBuilder();
    }

    public static ConnectTabPanelModuleBeanBuilder newTabPanelBean(ConnectTabPanelModuleBean defaultBean)
    {
        return new ConnectTabPanelModuleBeanBuilder(defaultBean);
    }
}
