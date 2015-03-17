package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;

/**
 * Defines an entity property to be indexed by JIRA. An entity property is a reference to a JSON object, which also defines it's type.
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#ENTITY_PROPERTY_INDEX_EXTRACTION_CONFIGURATION_EXAMPLE}
 * @schemaTitle Property Index
 * @since 1.0
 */
public class EntityPropertyIndexExtractionConfigurationBean
{
    /**
     * The objectName to the JSON data which is supposed to be indexed. The objectName will be the key of a flatten JSON object with '.' as the delimiter.
     *
     * For instance, for JSON <code>"{"label": {"color": "red", "text":"connect"}}</code> the valid objectName
     * referencing the color is label.color.
     *
     * Currently, specifying of index for JSON arrays is not supported.
     */
    @Required
    private String objectName;

    /**
     * The type of the referenced value.
     *
     * The type can be one of the following values:
     *
     * * `number`, which is indexed as a number and allows the range ordering and searching on this field.
     * * `text`, which is tokenized before indexing and allows searching for particular words.
     * * `string` which is indexed as is and allows searching for the exact phase only.
     * * `date`, which is indexed as a date and allows date range searching and ordering. The expected date format is [YYYY]-[MM]-[DD].
     * The expected date time format is [YYYY]-[MM]-[DD]T[hh]:[mm] with optional offset from UTC: +/-[hh]:[mm] or `Z` for no offset.
     * For reference, please see [ISO_8601 standard](http://www.w3.org/TR/NOTE-datetime).
     *
     */
    @Required
    private EntityPropertyIndexType type;

    /**
     * The name, under which this property will be searchable with JQL.
     */
    @Nullable
    private String alias;

    public EntityPropertyIndexExtractionConfigurationBean(String objectName, EntityPropertyIndexType type)
    {
        this(objectName, type, null);
    }

    public EntityPropertyIndexExtractionConfigurationBean(String objectName, EntityPropertyIndexType type, String alias)
    {
        this.objectName = objectName;
        this.type = type;
        this.alias = alias;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public EntityPropertyIndexType getType()
    {
        return type;
    }

    public String getAlias()
    {
        return alias;
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
                .append(objectName, other.objectName)
                .append(type, other.type)
                .append(alias, other.alias)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(53, 11)
                .append(objectName)
                .append(type)
                .append(alias)
                .build();
    }
}
