package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.EntityPropertyModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyType;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Entity properties allow add-ons to add key/value stores to JIRA entities, such as issues or projects.
 * These values are indexed by JIRA and able to be queried via a REST api or through JQL. For more information,
 * please see the [JIRA documentation on entity properties](https://developer.atlassian.com/display/JIRADEV/JIRA+Entity+Properties+Overview).
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#ENTITY_PROPERTY_EXAMPLE}
 * @schemaTitle Entity Property
 * @since 1.0
 */
public class EntityPropertyModuleBean extends RequiredKeyBean
{
    /**
     * List of properties from which selected values are indexed.
     */
    private List<EntityPropertyIndexKeyConfigurationBean> keyConfigurations;

    /**
     * The type of the entity. The default value is issue.
     */
    @CommonSchemaAttributes (defaultValue = "issue")
    private EntityPropertyType entityType;

    public EntityPropertyModuleBean()
    {
        this.keyConfigurations = Lists.newArrayList();
        this.entityType = EntityPropertyType.issue;
    }

    public EntityPropertyModuleBean(EntityPropertyModuleBeanBuilder builder)
    {
        super(builder);

        if (null == keyConfigurations)
        {
            this.keyConfigurations = Lists.newArrayList();
        }
        if (null == entityType)
        {
            this.entityType = EntityPropertyType.issue;
        }
    }

    public List<EntityPropertyIndexKeyConfigurationBean> getKeyConfigurations()
    {
        return keyConfigurations;
    }

    public EntityPropertyType getEntityType()
    {
        return entityType;
    }

    public static EntityPropertyModuleBeanBuilder newEntityPropertyModuleBean()
    {
        return new EntityPropertyModuleBeanBuilder();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(53, 11)
                .append(entityType)
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

        if (!(otherObj instanceof EntityPropertyModuleBean))
        {
            return false;
        }

        final EntityPropertyModuleBean other = (EntityPropertyModuleBean) otherObj;

        return new EqualsBuilder()
                .append(entityType, other.keyConfigurations)
                .append(entityType, other.entityType)
                .isEquals();
    }
}
