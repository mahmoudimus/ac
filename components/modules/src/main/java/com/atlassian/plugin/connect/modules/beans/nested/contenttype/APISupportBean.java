package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import java.util.Set;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.APISupportBeanBuilder;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Captures business logic for this Extensible Content Type.
 *
 * @since 1.1.77
 */
@SchemaDefinition("apiSupport")
public class APISupportBean extends BaseModuleBean
{
    /**
     * The body type of the Extensible Content Type. Defaults to {@code storage}
     */
    @CommonSchemaAttributes(defaultValue = "storage")
    private BodyType bodyType;

    /**
     * Defines types that this Extensible Content Type can be contained in.
     * {@code global} or/and {@code personal} should be set for a first class space content.
     *
     * For example: ["global", "blogpost"] indicates a global Space or a BlogPost can be set as the container of
     * this Extensible Content Type.
     */
    @Required
    private Set<String> supportedContainerTypes;

    /**
     * Defines types that can be contained in this Extensible Content Type
     *
     * For example: ["comment", "attachment"] indicates Comment and Attachment can be contained in this type.
     */
    private Set<String> supportedContainedTypes;

    @StringSchemaAttributes(format = "uri")
    @CommonSchemaAttributes(defaultValue = "")
    private String onCreateUrl;

    @StringSchemaAttributes(format = "uri")
    @CommonSchemaAttributes(defaultValue = "")
    private String onUpdateUrl;

    @StringSchemaAttributes(format = "uri")
    @CommonSchemaAttributes(defaultValue = "")
    private String onDeleteUrl;

    /**
     * Defines how would this Extensive Content Type be indexed
     */
    private IndexingBean indexing;

    public APISupportBean()
    {
        this(new APISupportBeanBuilder());
    }

    public APISupportBean(APISupportBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {
        supportedContainedTypes = ObjectUtils.defaultIfNull(supportedContainedTypes, Sets.newHashSet());
        indexing = ObjectUtils.defaultIfNull(indexing, new IndexingBean());
    }

    public BodyType getBodyType()
    {
        return bodyType;
    }

    public Set<String> getSupportedContainerTypes()
    {
        return supportedContainerTypes;
    }

    public Set<String> getSupportedContainedTypes()
    {
        return supportedContainedTypes;
    }

    public String getOnCreateUrl()
    {
        return onCreateUrl;
    }

    public String getOnUpdateUrl()
    {
        return onUpdateUrl;
    }

    public String getOnDeleteUrl()
    {
        return onDeleteUrl;
    }

    public IndexingBean getIndexing()
    {
        return indexing;
    }

}