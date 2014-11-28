package com.atlassian.plugin.connect.modules.beans.nested;

/**
 * Target format for macro render.  This list is based loosely off
 * {@link com.atlassian.confluence.content.render.xhtml.ConversionContextOutputType} (Confluence).
 *
 * @schemaTitle Macro Render Mode Type
 */
public enum MacroRenderModeType
{
    /**
     * Used when rendering is occurring in a Word document, such as from using Confluence's "Edit in Word" option.
     */
    WORD,

    /**
     * Used when rendering is occurring in a PDF document, such as from using Confluence's "Export to PDF" option.
     */
    PDF,

    /**
     * Used when rendering is occurring in an HTML export, such as from using Confluence's export space mechanism.
     */
    HTML_EXPORT,

    /**
     * Used when rendering is occurring in a feed, such as from using Confluence's built-in RSS feeds.
     */
    FEED,

    /**
     * Used when rendering is occurring in a notification, such as one received via email by "Watch"ing a Confluence page.
     */
    EMAIL,

    /**
     * This render mode will match WORD, PDF, HTML_EXPORT, FEED and EMAIL if they are not specified individually.
     */
    DEFAULT_FALLBACK;

    public String toString()
    {
        return this.name().toLowerCase();
    }

    public boolean isStatic()
    {
        switch (this)
        {
            case WORD:
            case PDF:
            case HTML_EXPORT:
            case FEED:
            case EMAIL:
            case DEFAULT_FALLBACK:
                return true;
        }
        return false;
    }

}
