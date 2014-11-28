package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.MacroRenderModesBeanBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * Describes a mapping between different render modes and a fallback static macro.
 *
 * Defining a mapping for these render modes allows your dynamic macro to render static content for output
 * types where this is possible.  This can allow you to render a view of your data to pdf export.
 *
 * You can define a mapping for each render mode or define a global fallback mapping to catch all
 * static render modes.
 *
 * #### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#MACRO_RENDER_MODES_EXAMPLE}
 * @schemaTitle Macro Render Modes
 * @since 1.1.15
 */
public class MacroRenderModesBean extends BaseModuleBean
{
    private static final String OUTPUT_WORD = "word";
    private static final String OUTPUT_PDF = "pdf";
    private static final String OUTPUT_HTML_EXPORT = "html_export";
    private static final String OUTPUT_FEED = "feed";
    private static final String OUTPUT_EMAIL = "email";

    /**
     * This render mode will be used when your macro is being rendered during "export to word"
     */
    private EmbeddedStaticContentMacroBean word;
    /**
     * This render mode will be used when your macro is being rendered during "export to pdf"
     */
    private EmbeddedStaticContentMacroBean pdf;
    /**
     * This render mode will be used when your macro is being rendered during "export to html"
     */
    private EmbeddedStaticContentMacroBean htmlExport;
    /**
     * This render mode will be used when your macro is being rendered in an rss feed
     */
    private EmbeddedStaticContentMacroBean feed;
    /**
     * This render mode will be used when your macro is being rendered in an email
     */
    private EmbeddedStaticContentMacroBean email;
    /**
     * This render mode will be used for any static render mode that is not mapped directly.  This is a catch
     * all mode which allows you to set a default static fallback for all render modes.
     */
    @SerializedName("default")
    private EmbeddedStaticContentMacroBean defaultFallback;

    protected MacroRenderModesBean()
    {
        super();
    }

    public MacroRenderModesBean(BaseModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static MacroRenderModesBeanBuilder newMacroRenderModesBean()
    {
        return new MacroRenderModesBeanBuilder();
    }

    /**
     * Return the {@link com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean} for the
     * output type specified by a {@link com.atlassian.confluence.content.render.xhtml.ConversionContext#getOutputType}
     * during a macro execution.
     *
     * @param outputType the output type to find the render mode for
     * @return the static macro fallback for the output type if one is found
     */
    public EmbeddedStaticContentMacroBean getEmbeddedStaticContentMacro(String outputType)
    {
        if (outputType.toLowerCase().equals(OUTPUT_WORD))
        {
            return fallbackFrom(word);
        }
        else if (outputType.toLowerCase().equals(OUTPUT_PDF))
        {
            return fallbackFrom(pdf);
        }
        else if (outputType.toLowerCase().equals(OUTPUT_HTML_EXPORT))
        {
            return fallbackFrom(htmlExport);
        }
        else if (outputType.toLowerCase().equals(OUTPUT_FEED))
        {
            return fallbackFrom(feed);
        }
        else if (outputType.toLowerCase().equals(OUTPUT_EMAIL))
        {
            return fallbackFrom(email);
        }
        return defaultFallback;
    }

    private EmbeddedStaticContentMacroBean fallbackFrom(EmbeddedStaticContentMacroBean bean)
    {
        return (bean == null) ? defaultFallback : bean;
    }
}
