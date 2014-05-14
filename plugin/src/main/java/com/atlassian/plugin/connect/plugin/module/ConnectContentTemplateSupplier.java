package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.confluence.plugins.createcontent.extensions.ContentTemplateSupplier;

/**
 * A content template supplier that allows the template to be loaded from a URL or inline.
 */
public class ConnectContentTemplateSupplier implements ContentTemplateSupplier {
    @Override
    public String getTemplateContent() {
        return "<h1>My Template</h1>";
    }
}
