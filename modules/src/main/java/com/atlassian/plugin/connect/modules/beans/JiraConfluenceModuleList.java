package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class JiraConfluenceModuleList extends JiraModuleList
{
    /////////////////////////////////////////////////////
    ///////    CONFLUENCE MODULES
    /////////////////////////////////////////////////////

    /**
     * Dynamic content macros allow you to add a macro into a Confluence page which is rendered as an iframe.
     *
     * @schemaTitle Dynamic Content Macro
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.DynamicContentMacroModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<DynamicContentMacroModuleBean> dynamicContentMacros;

    /**
     * The Space Tools Tab module allows you to add new tabs to the Space Tools area of Confluence.
     * @schemaTitle Space Tools Tab
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.SpaceToolsTabModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<SpaceToolsTabModuleBean> spaceToolsTabs;

    /**
     * Static content macros allow you to add a macro into a Confluence page which is stored with the Confluence page
     * itself. The add-on is responsible for generating the rendered XHTML in
     * [Confluence Storage Format](https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format)
     *
     * @schemaTitle Static Content Macro
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.StaticContentMacroModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<StaticContentMacroModuleBean> staticContentMacros;

    public JiraConfluenceModuleList()
    {
        this.dynamicContentMacros = newArrayList();
        this.spaceToolsTabs = newArrayList();
        this.staticContentMacros = newArrayList();
    }

    public JiraConfluenceModuleList(BaseModuleBeanBuilder builder)
    {
        super(builder);

        if (null == dynamicContentMacros)
        {
            this.dynamicContentMacros = newArrayList();
        }
        if (null == spaceToolsTabs)
        {
            this.spaceToolsTabs = newArrayList();
        }
        if (null == staticContentMacros)
        {
            this.staticContentMacros = newArrayList();
        }
    }

    public List<DynamicContentMacroModuleBean> getDynamicContentMacros()
    {
        return dynamicContentMacros;
    }

    public List<SpaceToolsTabModuleBean> getSpaceToolsTabs() {
        return spaceToolsTabs;
    }

    public List<StaticContentMacroModuleBean> getStaticContentMacros()
    {
        return staticContentMacros;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof JiraConfluenceModuleList))
        {
            return false;
        }

        JiraConfluenceModuleList other = (JiraConfluenceModuleList) otherObj;

        return new EqualsBuilder()
                .appendSuper(super.equals(otherObj))
                .append(dynamicContentMacros, other.dynamicContentMacros)
                .append(spaceToolsTabs, other.spaceToolsTabs)
                .append(staticContentMacros, other.staticContentMacros)
                .build();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(29, 37)
                .append(dynamicContentMacros)
                .append(spaceToolsTabs)
                .append(staticContentMacros)
                .build();
    }

}
