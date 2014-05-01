package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.SpaceToolsTabModuleBeanBuilder;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Space Tools Tab modules enable add-ons to insert tabs into Confluence Space Tools area. These can be automatically
 * resized to the width and height of your add-on's content. The location attribute defines which section the tab will
 * appear.
 *
 * Spaces page with the Documentation Theme do not support the Space Tools area, and instead display the
 * legacy Space Admin area. This module will insert a tab in a pre-defined location in Space Admin (look below for a 
 * complete list of existing locations).
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#SPACE_TOOLS_TAB_EXAMPLE}
 * @since 1.0
 */
public class SpaceToolsTabModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * The url to retrieve the content from.
     * This can be absolute or relative to the addon's baseUrl
     */
    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String url;


    @CommonSchemaAttributes (defaultValue = "100")
    private Integer weight;

    private String location;

    public SpaceToolsTabModuleBean()
    {
        init();
    }

    public SpaceToolsTabModuleBean(SpaceToolsTabModuleBeanBuilder builder)
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
     *The sub-section where this Space Tools Tab should appear. The primary section for Space Tabs is
     * `system.space.tools`, and the location specified here will be beneath that. e.g setting a location of
     * "contenttools" will result in a final location of `system.space.tools/contenttools`.
     *
     *Confluence comes the following standard sections in Space Tools:
     *
     * * system.space.tools/overview
     * * system.space.tools/permissions
     * * system.space.tools/contenttools
     * * system.space.tools/lookandfeel
     * * system.space.tools/integrations
     * * system.space.tools/addons
     *
     *In future, Connect addons will be able to define new web sections, which will enable add-ons to define new
     * sub-sections for Space Tools.
     *
     *Legacy Space Admin section cannot be defined, and is always system.space.admin/spaceops
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
                .add("location", getLocation());
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof SpaceToolsTabModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        SpaceToolsTabModuleBean other = (SpaceToolsTabModuleBean) otherObj;

        return new EqualsBuilder()
                .append(url, other.url)
                .append(weight, other.weight)
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
                .append(location)
                .build();
    }

    public static SpaceToolsTabModuleBeanBuilder newSpaceToolsTabBean()
    {
        return new SpaceToolsTabModuleBeanBuilder();
    }

    public static SpaceToolsTabModuleBeanBuilder newSpaceToolsTabBean(SpaceToolsTabModuleBean defaultBean)
    {
        return new SpaceToolsTabModuleBeanBuilder(defaultBean);
    }
}
