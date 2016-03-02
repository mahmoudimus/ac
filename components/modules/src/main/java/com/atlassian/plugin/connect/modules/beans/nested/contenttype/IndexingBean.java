package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples;

/**
 * Defines how this content type will be indexed
 *
 * @schemaTitle Indexing
 * @exampleJson {@link ConnectJsonExamples#EXTENSIBLE_CONTENT_TYPE_INDEXING_EXAMPLE}
 *
 * <p>The <code>contentPropertyBody</code> property allow add-on to hook into the mechanism by which Confluence populates its
 * search index. Each time an Extensible Content Type is created or updated in Confluence, the content that stored in
 * the content property defined in the <code>contentPropertyBody</code> will be added to the search index.</p>
 *
 * <p>This is useful when the body of the Extensible Content Type is not searchable, for example: JSON or binary data.
 * The add-on can still provide meaningful search text for this content via storing the extracted information to the
 * content property defined in <code>contentPropertyBody</code>.</p>
 *
 * <p>Please consult <a href="https://developer.atlassian.com/confdev/confluence-rest-api/content-properties-in-the-rest-api">Content Properties in the REST API</a>
 * for how to store, modify and delete content property via REST API.</p>
 *
 * @since 1.1.77
 */
@SchemaDefinition("indexing")
public class IndexingBean {
    /**
     * Defines whether this Extensive Content Type should be indexed. Defaults to {@code true}.
     */
    private boolean enabled;

    /**
     * Defines the key of content property that should be appended to the body text that is indexed by Extractors.
     * This allows add-on to add additional information to default text in Confluence search.
     */
    private String contentPropertyBody;

    public IndexingBean() {
        this(true, "");
    }

    public IndexingBean(boolean enabled, String contentPropertyBody) {
        this.enabled = enabled;
        this.contentPropertyBody = contentPropertyBody;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getContentPropertyBody() {
        return contentPropertyBody;
    }
}
