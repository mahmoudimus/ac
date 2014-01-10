package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a reference to the value from JSON object and the type of the referenced data.
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#ENTITY_PROPERTY_INDEX_EXTRACTION_CONFIGURATION_EXAMPLE}
 * @schemaTitle Index Extraction
 * @since 1.0
 */
public class EntityPropertyIndexExtractionConfigurationBean
{
    /**
     * The path to the JSON data which is supposed to be indexed. The path will be the key of a flatten JSON object with '.' as the delimiter.
     *
     * For instance, for JSON "{"label": {"color": "red", "text":"connect"}} the valid path
     * referencing the color is label.color.
     *
     * Currently, specifying of index for JSON arrays is not supported.
     */
    @Required
    private String path;

    /**
     * The type of the referenced value.
     */
    @Required
    private EntityPropertyIndexType type;

    public EntityPropertyIndexExtractionConfigurationBean(String path, EntityPropertyIndexType type)
    {
        this.path = path;
        this.type = type;
    }

    public String getPath()
    {
        return path;
    }

    public EntityPropertyIndexType getType()
    {
        return type;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof EntityPropertyIndexExtractionConfigurationBean))
        {
            return false;
        }

        EntityPropertyIndexExtractionConfigurationBean other = (EntityPropertyIndexExtractionConfigurationBean) otherObj;

        return new EqualsBuilder()
                .append(path, other.path)
                .append(type, other.type)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(53, 11)
                .append(path)
                .append(type)
                .build();
    }
}
