package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.json.schema.annotation.SchemaDefinition;

import com.google.common.collect.Lists;

/**
 * Defines how would the Extensive Content Type be indexed
 *
 * @since 1.1.77
 */
@SchemaDefinition("indexing")
public class IndexingBean
{
    /**
     * Defines whether the Extensive Content Type should be indexed. Defaults to {@code true}
     */
    private boolean enabled;

    /**
     * Defines the key of content property that should be appended to the body text that is indexed by Extractors.
     * This allows add-on to add additional information to default text in Confluence search
     */
    private String contentPropertyBody;

    public IndexingBean()
    {
        this(true, "");
    }

    public IndexingBean(boolean enabled, String contentPropertyBody)
    {
        this.enabled = enabled;
        this.contentPropertyBody = contentPropertyBody;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public String getContentPropertyBody()
    {
        return contentPropertyBody;
    }
}
