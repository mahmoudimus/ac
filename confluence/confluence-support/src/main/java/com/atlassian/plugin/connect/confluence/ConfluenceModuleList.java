package com.atlassian.plugin.connect.confluence;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;

import java.util.List;

/**
 * A container class used for generation of JSON schema for Confluence modules.
 */
@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class ConfluenceModuleList extends BaseModuleBean
{
    /**
     * Dynamic content macros allow you to add a macro into a Confluence page which is rendered as an iframe.
     *
     */
    private List<DynamicContentMacroModuleBean> dynamicContentMacros;

    /**
     * A User Profile Page module is used to add new elements to Confluence user profiles.
     */
    private List<ConnectPageModuleBean> profilePages;

    /**
     * The Space Tools Tab module allows you to add new tabs to the Space Tools area of Confluence.
     */
    private List<SpaceToolsTabModuleBean> spaceToolsTabs;

    /**
     * Static content macros allow you to add a macro into a Confluence page which is stored with the Confluence page
     * itself. The add-on is responsible for generating the rendered XHTML in
     * [Confluence Storage Format](https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format)
     */
    private List<StaticContentMacroModuleBean> staticContentMacros;

    /**
     * Blueprints allow your connect add on provide content creation templates.
     */
    private List<BlueprintModuleBean> blueprints;

    /**
     * Definition of a content property index schema for an add-on. It allows extracting specific parts of the JSON
     * documents stored as a content property values, and write them to a search index. Once stored,
     * they can participate in a content search using CQL.
     */
    private List<ContentPropertyModuleBean> confluenceContentProperties;

    private ConfluenceModuleList()
    {
    }
}
