package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.EntityPropertyIndexDocumentModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyType;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Entity Property Index Document allows add-ons to index selected values from JSON properties stored against issues. For the
 * documentation of available REST resources available for issue properties, please check https://docs.atlassian.com/jira/REST/ondemand/#d2e529.
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#ENTITY_PROPERTY_INDEX_DOCUMENT_EXAMPLE}
 * @schemaTitle Entity Property Index Document
 * @since 1.0
 */
public class EntityPropertyIndexDocumentModuleBean extends NameToKeyBean
{

    /**
     * List of properties from which selected values are indexed.
     */
    private List<EntityPropertyIndexKeyConfigurationBean> keyConfigurations;

    /**
     * The key
     */
    @CommonSchemaAttributes (defaultValue = "issue")
    private EntityPropertyType propertyType;

    public EntityPropertyIndexDocumentModuleBean()
    {
        this.keyConfigurations = Lists.newArrayList();
        this.propertyType = EntityPropertyType.issue;
    }

    public EntityPropertyIndexDocumentModuleBean(EntityPropertyIndexDocumentModuleBeanBuilder builder)
    {
        super(builder);

        if (null == keyConfigurations)
        {
            this.keyConfigurations = Lists.newArrayList();
        }
        if (null == propertyType)
        {
            this.propertyType = EntityPropertyType.issue;
        }
    }

    public List<EntityPropertyIndexKeyConfigurationBean> getKeyConfigurations()
    {
        return keyConfigurations;
    }

    public EntityPropertyType getPropertyType()
    {
        return propertyType;
    }

    public static EntityPropertyIndexDocumentModuleBeanBuilder newEntityPropertyIndexDocumentModuleBean()
    {
        return new EntityPropertyIndexDocumentModuleBeanBuilder();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(53, 11)
                .append(propertyType)
                .append(keyConfigurations)
                .build();
    }

    @Override
    public boolean equals(final Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof EntityPropertyIndexDocumentModuleBean))
        {
            return false;
        }

        final EntityPropertyIndexDocumentModuleBean other = (EntityPropertyIndexDocumentModuleBean) otherObj;

        return new EqualsBuilder()
                .append(propertyType, other.keyConfigurations)
                .append(propertyType, other.propertyType)
                .isEquals();
    }
}
