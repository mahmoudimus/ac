package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebSectionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The Web Section plugin module allows add-ons to define new sections in application menus. Each section can contain one or
 * more links. To insert the links themselves, see the [Web Item Module](./web-item.html).
 * <p/>
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBSECTION_EXAMPLE}
 * @schemaTitle Web Section
 * @since 1.0
 */
@SchemaDefinition("webSection")
public class WebSectionModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * The location in the application interface where the web section should appear. For the Atlassian application
     * interface, a location is something like the coordinates on a map. It points to a particular drop-down menu or
     * navigation list in the UI.
     * 
     * Product location documentation:
     * 
     * * [JIRA locations](https://developer.atlassian.com/display/JIRADEV/Web+Fragments)
     * * [Confluence locations](https://developer.atlassian.com/display/CONFDEV/Web+UI+Modules)
     */ 
    @Required
    private String location;

    /**
     * The internationalised text to be used in the link's tooltip.
     */
    private I18nProperty tooltip;

    /**
     * Determines the order in which the web section appears in the menu or list.
     * <p/>
     * The "lightest" weight (i.e., lowest number) appears first, rising relative to other items, while the "heaviest"
     * weights sink to the bottom of the menu or list.
     * <p/>
     * Built-in web sections have weights that are incremented by numbers that leave room for additional sections, such
     * as by 10 or 100. Be mindful of the weight you choose for your item, so that it appears in a sensible order given
     * existing items.
     */
    @CommonSchemaAttributes (defaultValue = "100")
    private Integer weight;


    public WebSectionModuleBean()
    {
        this.location = "";
        this.weight = 100;
        this.tooltip = null;
    }

    public WebSectionModuleBean(WebSectionModuleBeanBuilder builder)
    {
        super(builder);

        if (null == weight)
        {
            this.weight = 100;
        }
    }

    public String getLocation()
    {
        return location;
    }

    public int getWeight()
    {
        return weight;
    }

    public I18nProperty getTooltip()
    {
        return tooltip;
    }

    public static WebSectionModuleBeanBuilder newWebSectionBean()
    {
        return new WebSectionModuleBeanBuilder();
    }

    public static WebSectionModuleBeanBuilder newWebSectionBean(WebSectionModuleBean defaultBean)
    {
        return new WebSectionModuleBeanBuilder(defaultBean);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("key", getRawKey())
                .add("name", getName())
                .add("location", getLocation())
                .add("weight", getWeight())
                .add("tooltip", getTooltip())
                .add("conditions", getConditions())
                .add("params", getParams())
                .toString();
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof WebSectionModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        WebSectionModuleBean other = (WebSectionModuleBean) otherObj;

        return new EqualsBuilder()
                .append(location, other.location)
                .append(weight, other.weight)
                .append(tooltip, other.tooltip)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 61)
                .appendSuper(super.hashCode())
                .append(location)
                .append(weight)
                .append(tooltip)
                .build();
    }
}
