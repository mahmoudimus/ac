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
     * Defines the keys of content property that should be appended to the body text that is indexed by Extractors.
     * This allows add-on to add additional information to default text in Confluence search
     */
    private List<String> contentPropertyBody;

    public IndexingBean()
    {
        this(true, Lists.newArrayList());
    }

    public IndexingBean(boolean enabled, ArrayList<String> contentPropertyBody)
    {
        this.enabled = enabled;
        this.contentPropertyBody = contentPropertyBody;
    }
}
