package com.atlassian.plugin.connect.modules.beans.nested;

/**
 * Target format for macro render.  This list is based loosely off
 * {@link com.atlassian.confluence.content.render.xhtml.ConversionContextOutputType} (Confluence).
 *
 * @schemaTitle Macro Render Mode Type
 */
public enum MacroRenderModeType
{
    /*
     * Indicates your macro should output for mobile rendering.
     *
     * Note: Currently disabled, see AC-1210 and ACDEV-1400
     */
//    mobile, /** Currently the ConversionContextOutputType doesn't cover mobile, it may be rendering as DISPLAY **/

//    /**
//     * Indicates your macro should out for desktop rendering.
//     *
//     * This is the default if a render mode is not specified.
//     */
//    desktop,

    /**
     * Used when rendering is occurring in display mode, such as when viewing a Confluence page. This is the default type.
     */
    DISPLAY, // Renamed from 'desktop' to be inline with Confluence

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
    STATIC;

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
            case STATIC:
                return true;
        }
        return false;
    }

}
