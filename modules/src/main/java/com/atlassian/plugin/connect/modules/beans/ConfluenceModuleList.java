package com.atlassian.plugin.connect.modules.beans;


import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedListWithType;
import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class ConfluenceModuleList extends BaseModuleBean
{
    /**
     * Dynamic content macros allow you to add a macro into a Confluence page which is rendered as an iframe.
     *
     * @schemaTitle Dynamic Content Macro
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.confluence.capabilities.provider.DynamicContentMacroModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<DynamicContentMacroModuleBean> dynamicContentMacros;

    /**
     * A User Profile Page module is used to add new elements to Confluence user profiles.
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.confluence.capabilities.provider.ProfilePageModuleProvider", products = {ProductFilter.CONFLUENCE}) // Note: Jira uses jiraProfileTabPanels instead
    private List<ConnectPageModuleBean> profilePages;

    /**
     * The Space Tools Tab module allows you to add new tabs to the Space Tools area of Confluence.
     * @schemaTitle Space Tools Tab
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.confluence.capabilities.provider.SpaceToolsTabModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<SpaceToolsTabModuleBean> spaceToolsTabs;

    /**
     * Static content macros allow you to add a macro into a Confluence page which is stored with the Confluence page
     * itself. The add-on is responsible for generating the rendered XHTML in
     * [Confluence Storage Format](https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format)
     *
     * @schemaTitle Static Content Macro
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.confluence.capabilities.provider.StaticContentMacroModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<StaticContentMacroModuleBean> staticContentMacros;

    /**
     * Blueprints allow your connect add on provide content creation templates.
     *
     * @schemaTitle Blueprint
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.confluence.capabilities.provider.DefaultBlueprintModuleProvider", products = {ProductFilter.CONFLUENCE})
    private List<BlueprintModuleBean> blueprints;

    /**
     * Definition of a content property index schema for an add-on. It allows extracting specific parts of the JSON
     * documents stored as a content property values, and write them to a search index. Once stored,
     * they can participate in a content search using CQL.
     *
     * @see <a href="https://developer.atlassian.com/display/CONFDEV/Content+Properties+in+the+REST+API">
     *     developer.atlassian.com</a> for more details
     */
    @ConnectModule (value = "com.atlassian.plugin.connect.confluence.capabilities.provider.DefaultContentPropertyModuleProvider", products = { ProductFilter.CONFLUENCE })
    private List<ContentPropertyModuleBean> confluenceContentProperties;


    public ConfluenceModuleList()
    {
        this.dynamicContentMacros = newArrayList();
        this.profilePages = newArrayList();
        this.spaceToolsTabs = newArrayList();
        this.staticContentMacros = newArrayList();
        this.blueprints = newArrayList();
        this.confluenceContentProperties = newArrayList();
    }

    public ConfluenceModuleList(BaseModuleBeanBuilder builder)
    {
        super(builder);

        if (null == profilePages)
        {
            this.profilePages = newArrayList();
        }
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
        if (null == blueprints)
        {
            this.blueprints = newArrayList();
        }
        if (null == confluenceContentProperties)
        {
            this.confluenceContentProperties = newArrayList();
        }
    }

    public List<ConnectPageModuleBean> getProfilePages()
    {
        return profilePages;
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

    public List<BlueprintModuleBean> getBlueprints() {
        return blueprints;
    }

    public List<ContentPropertyModuleBean> getConfluenceContentProperties()
    {
        return confluenceContentProperties;
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ModuleList))
        {
            return false;
        }

        ConfluenceModuleList other = (ConfluenceModuleList) otherObj;

        return new EqualsBuilder()
                .append(dynamicContentMacros, other.dynamicContentMacros)
                .append(profilePages, other.profilePages)
                .append(spaceToolsTabs, other.spaceToolsTabs)
                .append(staticContentMacros, other.staticContentMacros)
                .append(blueprints, other.blueprints)
                .build();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(29, 37)
                .append(dynamicContentMacros)
                .append(profilePages)
                .append(spaceToolsTabs)
                .append(staticContentMacros)
                .append(blueprints)
                .build();
    }

    public boolean isEmpty()
    {
        for (Field field : getClass().getDeclaredFields())
        {
            if (field.isAnnotationPresent(ConnectModule.class))
            {
                try
                {
                    ConnectModule anno = field.getAnnotation(ConnectModule.class);
                    field.setAccessible(true);

                    Type fieldType = field.getGenericType();

                    List<? extends ModuleBean> beanList;

                    if (isParameterizedListWithType(fieldType, ModuleBean.class))
                    {
                        beanList = (List<? extends ModuleBean>) field.get(this);
                    }
                    else
                    {
                        ModuleBean moduleBean = (ModuleBean) field.get(this);
                        beanList = moduleBean == null ? Collections.<ModuleBean>emptyList() : newArrayList(moduleBean);
                    }

                    if(!beanList.isEmpty())
                    {
                        return false;
                    }

                }
                catch (IllegalAccessException e)
                {
                    //ignore. this should never happen
                }
            }
        }

        return true;
    }
    
}
