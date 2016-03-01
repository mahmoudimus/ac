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
 * <p>Entity properties allow add-ons to add key/value stores to JIRA entities, such as issues or projects.
 * This module allows you to request those entity properties to be indexed by JIRA and able to be queried via JQL searches.
 * They are also available in the <i>entity_property_equal_to</i> condition.</p>
 *
 * <h3>Overview</h3>
 *
 * <p>The purpose of this module is to specify what data from which entity properties should be extracted and indexed.
 * Pretend that an add-on developer has an issue entity property with the key 'attachment' and in that entity property they store the following
 * data:</p>
 *
 * <pre>
 * {
 *     "attachment": {
 *         "size": 14231,
 *         "name": "new-years-jam",
 *         "extension": "mp3",
 *         "updated": "2016-12-25T20:55:59"
 *     },
 *     "extraData": {
 *         ...
 *     }
 * }
 * </pre>
 *
 * <p>In this example the developer wants to make the <i>size</i>, <i>extension</i> and <i>updated</i> fields from the <i>attachment</i> object be searchable via JQL. To do that they start
 * by declaring that the <i>entityType</i> to index will be an 'issue' entity type; this is specified at the top level of their
 * module. Then they need to specify which entity property key that they wish to extract data from: so they add a single entry
 * to <i>keyConfiguratons</i> with the <i>propertyKey</i> of 'attachment'. If there are multiple issue entity properties that an add-on developer wanted
 * to index then they could add more <i>keyConfigurations</i> to declare those extra properties. From there the add-on developer specifies
 * which data they want to extract from the json value that is stored in this issue entity property. In this example they would
 * add three extractions for <i>attachment.size</i>, <i>attachment.extension</i> and <i>attachment.updated</i> being clear to specify the typo
 * of data being extracted and what alias should be made avaliable to JQL queries.</p>
 *
 * <p>It is important to note that array types can be indexed too; that the <i>type</i> field in the extraction should be the type of
 * each element in the array.</p>
 *
 * <p>You can see the resultant module definition in the example below.</p>
 *
 * <p>For more information, please see the <a href="https://developer.atlassian.com/display/JIRADEV/JIRA+Entity+Properties+Overview">JIRA documentation on entity properties</a>.</p>
 *
 * <h4>Example</h4>
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#ENTITY_PROPERTY_EXAMPLE}
 * @schemaTitle Entity Property
 * @since 1.0
 */
public class EntityPropertyModuleBean extends RequiredKeyBean {
    /**
     * List of properties from which selected values are indexed.
     */
    private List<EntityPropertyIndexKeyConfigurationBean> keyConfigurations;

    /**
     * The type of the entity. The default value is issue.
     */
    @CommonSchemaAttributes(defaultValue = "issue")
    private EntityPropertyType entityType;

    public EntityPropertyModuleBean() {
        this.keyConfigurations = Lists.newArrayList();
        this.entityType = EntityPropertyType.issue;
    }

    public EntityPropertyModuleBean(EntityPropertyModuleBeanBuilder builder) {
        super(builder);

        if (null == keyConfigurations) {
            this.keyConfigurations = Lists.newArrayList();
        }
        if (null == entityType) {
            this.entityType = EntityPropertyType.issue;
        }
    }

    public List<EntityPropertyIndexKeyConfigurationBean> getKeyConfigurations() {
        return keyConfigurations;
    }

    public EntityPropertyType getEntityType() {
        return entityType;
    }

    public static EntityPropertyModuleBeanBuilder newEntityPropertyModuleBean() {
        return new EntityPropertyModuleBeanBuilder();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 11)
                .append(entityType)
                .append(keyConfigurations)
                .build();
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (otherObj == this) {
            return true;
        }

        if (!(otherObj instanceof EntityPropertyModuleBean)) {
            return false;
        }

        final EntityPropertyModuleBean other = (EntityPropertyModuleBean) otherObj;

        return new EqualsBuilder()
                .append(entityType, other.keyConfigurations)
                .append(entityType, other.entityType)
                .isEquals();
    }
}
