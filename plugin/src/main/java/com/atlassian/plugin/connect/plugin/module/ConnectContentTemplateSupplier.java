package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.confluence.plugins.createcontent.extensions.ContentTemplateSupplier;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.NoOpContextProvider;

/**
 * A content template supplier that allows the template to be loaded from a URL or inline.
 */
public class ConnectContentTemplateSupplier implements ContentTemplateSupplier {
    @Override
    public String getTemplateContent() {
        return "<h1>My Template</h1>";
    }

    @Override
    public ContextProvider getContextProvider() {
        return new NoOpContextProvider();
    }

    @Override
    public void enabled() {
        // nothing to do here
    }

    @Override
    public void disabled() {
        // nothing to do here
    }
}
