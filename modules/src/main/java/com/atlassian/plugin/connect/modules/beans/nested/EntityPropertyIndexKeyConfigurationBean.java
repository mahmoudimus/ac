package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Defines the list of extractors which index selected JSON objects from defined property.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#ENTITY_PROPERTY_INDEX_KEY_CONFIGURATION_EXAMPLE}
 * @schemaTitle Index Key Configuration
 * @since 1.0
 */
public class EntityPropertyIndexKeyConfigurationBean
{
    /**
     * The list with references to values of JSON object which will be indexed and the types of referenced values.
     */
    @Required
    private List<EntityPropertyIndexExtractionConfigurationBean> extractions;

    /**
     * The key of the property from which the data is indexed.
     */
    @Required
    private String propertyKey;

    public EntityPropertyIndexKeyConfigurationBean(List<EntityPropertyIndexExtractionConfigurationBean> extractions, String propertyKey)
    {
        this.extractions = extractions;
        this.propertyKey = propertyKey;
    }

    public List<EntityPropertyIndexExtractionConfigurationBean> getExtractions()
    {
        return extractions;
    }

    public String getPropertyKey()
    {
        return propertyKey;
    }

    @Override
    public boolean equals(final Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof EntityPropertyIndexKeyConfigurationBean))
        {
            return false;
        }

        final EntityPropertyIndexKeyConfigurationBean other = (EntityPropertyIndexKeyConfigurationBean) otherObj;

        return new EqualsBuilder()
                .append(extractions, other.extractions)
                .append(propertyKey, other.propertyKey)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(53, 11)
                .append(extractions)
                .append(propertyKey)
                .build();
    }
}
