package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.MacroRenderModesBeanBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * Allows your dynamic content macro to provide different static macro implementations for different render modes.
 *
 * Dynamic Content Macros can include style sheets and javascript, allowing the development of rich interactive
 * applications.  When your macro is rendered in a web browser this can provide a modern, interactive web experience.
 *
 * When your macro is rendered to static formats such as PDF, or word, these interactive modes are
 * often undesirable, or technically impossible.
 *
 * Macro Render Modes allow you to map a render mode to a static content macro.  This allows you to
 * provide an implementation of your macro for these formats, that will render safely to static formats.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#MACRO_RENDER_MODES_EXAMPLE}
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
     * This render mode will be used when your macro is being rendered during "export to word".
     * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#RENDER_MODE_EXAMPLE_WORD}
     */
    private EmbeddedStaticContentMacroBean word;
    /**
     * This render mode will be used when your macro is being rendered during "export to pdf".
     * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#RENDER_MODE_EXAMPLE_PDF}
     */
    private EmbeddedStaticContentMacroBean pdf;

    /**
     * This render mode will be used for any static render mode that is not mapped directly.  This is a catch
     * all mode which allows you to set a default static fallback for all render modes.
     * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#RENDER_MODE_EXAMPLE_DEFAULT}
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
     * output type specified by a com.atlassian.confluence.content.render.xhtml.ConversionContext#getOutputType
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
        return null;
    }

    private EmbeddedStaticContentMacroBean fallbackFrom(EmbeddedStaticContentMacroBean bean)
    {
        return (bean == null) ? defaultFallback : bean;
    }
}
