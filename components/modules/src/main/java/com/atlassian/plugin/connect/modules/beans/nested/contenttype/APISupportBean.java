package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.APISupportBeanBuilder;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Set;

/**
 * Captures business logic for this Extensible Content Type.
 *
 * @since 1.1.77
 */
@SchemaDefinition("apiSupport")
public class APISupportBean extends BaseModuleBean {
    /**
     * Defines types that this Extensible Content Type can be contained in.
     * For example: ["space", "blogpost"] indicates a Space or a BlogPost can be set as the container of this Extensible Content Type.
     */
    @Required
    private Set<String> supportedContainerTypes;

    /**
     * Defines types that can be contained in this Extensible Content Type
     * <p>
     * For example: ["comment", "attachment"] indicates Comment and Attachment can be contained in this type.
     */
    private Set<String> supportedContainedTypes;

    /**
     * Defines how this content type will be indexed
     */
    private IndexingBean indexing;

    public APISupportBean() {
        this(new APISupportBeanBuilder());
    }

    public APISupportBean(APISupportBeanBuilder builder) {
        super(builder);
        initialise();
    }

    private void initialise() {
        supportedContainedTypes = ObjectUtils.defaultIfNull(supportedContainedTypes, Sets.newHashSet());
        indexing = ObjectUtils.defaultIfNull(indexing, new IndexingBean());
    }

    public Set<String> getSupportedContainerTypes() {
        return supportedContainerTypes;
    }

    public Set<String> getSupportedContainedTypes() {
        return supportedContainedTypes;
    }

    public IndexingBean getIndexing() {
        return indexing;
    }

}