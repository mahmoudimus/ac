package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;

/**
 * A Confluence macro that loads remote content as an iframe. Dynamic Content Macros render content on every page
 * request and are suitable for add-ons that need to display content that changes over time, that calls for dynamic
 * interaction, or that is specific to the authenticated user.
 *
 * Since Dynamic Content Macros are rendered in an iframe, you are able to include your own style sheets and javascript.
 * You can use these to create a rich, interactive experience for your users. When your macro is exported to a static
 * format such as PDF or Word, you can use the `renderModes` property to define a mapping between a certain type of output
 * device and a static macro implementation.  This will allow you to create a static view of your macro's data where an
 * interactive model is not appropriate.
 *
 * For most modules, you do not need to be concerned with iframe sizing. It's all handled for you. However, an exception
 * exists for inline macros.
 *
 * An inline macro is a type of macro that generates content within the text flow of a paragraph or other text element
 * in which the macro appears, such as a status lozenge. To implement an inline macro, follow these general guidelines:
 *
 * 1. In your `macro-page` declaration in the add-on descriptor, set the `output-type` attribute to `inline`. (Alternatively, if this value is set to `block`, the macro content will appear on a new line in the page output.)
 * 2. If the output content should occupy a certain width and height, set those values as the width and height attributes for the element.
 * 3. To prevent the macro output from being automatically resized, set the `data-options` attribute in the script tag for all.js to "`resize:false`". This turns off automatic resizing of the iframe.
 * 4. If the size of the macro output content size is dynamic, call `AP.resize(w,h)` immediately after the DOM of your iframe is loaded.
 *
 *#### Example
 * The following macro example is an adaptation from the [Google Maps](https://marketplace.atlassian.com/plugins/atlassian-connect-gmaps)
 * add-on. The source is hosted on [Bitbucket](https://bitbucket.org/atlassianlabs/ac-gmaps).
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#DYNAMIC_MACRO_EXAMPLE}
 * @schemaTitle Dynamic Content Macro
 * @since 1.0
 */
@SchemaDefinition("dynamicContentMacro")
public class DynamicContentMacroModuleBean extends BaseContentMacroModuleBean
{
    /**
     * The preferred width of the macro content.
     */
    private String width;

    /**
     * The preferred height of the macro content.
     */
    private String height;


    /**
     * Whenever macro is allowed to run in full screen or not.
     */
    private boolean allowfullscreen;

    /**
     * Since Dynamic Content Macros are rendered in an iframe, you are able to include your own style sheets and javascript.
     * When your macro is exported to a static format such as PDF or Word, you can use the `renderModes` property to
     * define a mapping between a certain type of output device and a static macro implementation.  This will allow you
     * to create a static view of your macro's data where an interactive model is not appropriate.
     */
    private MacroRenderModesBean renderModes;

    public DynamicContentMacroModuleBean()
    {
    }

    public DynamicContentMacroModuleBean(DynamicContentMacroModuleBeanBuilder builder)
    {
        super(builder);
        if (renderModes == null)
        {
            renderModes = MacroRenderModesBean.newMacroRenderModesBean().build();
        }
    }

    public MacroRenderModesBean getRenderModes()
    {
        return renderModes;
    }

    public String getWidth()
    {
        return width;
    }

    public String getHeight()
    {
        return height;
    }


    public boolean isAllowfullscreen()
    {
        return allowfullscreen;
    }

    public static DynamicContentMacroModuleBeanBuilder newDynamicContentMacroModuleBean()
    {
        return new DynamicContentMacroModuleBeanBuilder();
    }

}
