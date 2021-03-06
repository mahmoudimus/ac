package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

/**
 * @since 1.0
 */
public interface ConfluenceModuleContextParameters extends ModuleContextParameters {
    void addPage(AbstractPage page);

    void addSpace(Space space);

    void addContent(ContentEntityObject content);
}
