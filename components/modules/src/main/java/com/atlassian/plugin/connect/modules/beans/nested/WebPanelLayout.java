package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.SchemaDefinition;

/**
 * <h4>Example</h4>
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#PANEL_LAYOUT_EXAMPLE}
 * @schemaTitle Web Panel Layout
 * @since 1.0
 */
@SchemaDefinition("webPanelLayout")
public class WebPanelLayout {
    private final String width;
    private final String height;

    public WebPanelLayout() {
        this.width = "";
        this.height = "";
    }

    public WebPanelLayout(String width, String height) {
        this.width = width;
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }
}
