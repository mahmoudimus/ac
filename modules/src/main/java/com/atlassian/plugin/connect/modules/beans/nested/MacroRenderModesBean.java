package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.MacroRenderModesBeanBuilder;

/**
 * Describes a mapping between different render modes and a fallback static macro.
 *
 * Defining a mapping for these render modes allows your dynamic macro to render static content for output
 * types where this is possible.  This can allow you to render a view of your data to pdf export.
 *
 * You can define a mapping for each render mode or define a global fallback mapping to catch all
 * static render modes.
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#MACRO_RENDER_MODES_EXAMPLE}
 * @schemaTitle Macro Render Modes
 * @since 1.1.15
 */
public class MacroRenderModesBean extends BaseModuleBean
{
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

    public boolean hasMode(MacroRenderModeType type)
    {
        switch(type) {
            case EMAIL: return email != null;
            case FEED: return feed != null;
            case HTML_EXPORT: return htmlExport != null;
            case PDF: return pdf != null;
            case WORD: return word != null;
            case DEFAULT_FALLBACK: return defaultFallback != null;
        }
        return false;
    }

    public String getUrl(MacroRenderModeType type)
    {
        switch(type) {
            case EMAIL: return email.getUrl();
            case FEED: return feed.getUrl();
            case HTML_EXPORT: return htmlExport.getUrl();
            case PDF: return pdf.getUrl();
            case WORD: return word.getUrl();
            case DEFAULT_FALLBACK: return defaultFallback.getUrl();
        }
        return null;
    }
}
