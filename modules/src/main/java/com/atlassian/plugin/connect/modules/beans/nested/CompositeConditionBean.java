package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.builder.CompositeConditionBeanBuilder;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite Conditions are composed of a collection of [Single Condition](single-condition.html) / Composite Conditions
 * and a type attribute.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#COMPOSITE_CONDITION_EXAMPLE}
 * @schemaTitle Composite Condition
 * @since 1.0
 */
@SchemaDefinition("compositeCondition")
public class CompositeConditionBean extends BaseModuleBean implements ConditionalBean
{

    /**
     * The conditions to compose using the specific logical operator.
     */
    private List<ConditionalBean> conditions;

    /**
     * Defines what logical operator is used to evaluate its collection of condition elements.
     */
    private CompositeConditionType type;

    public CompositeConditionBean()
    {
        this.conditions = new ArrayList<ConditionalBean>();
        this.type = CompositeConditionType.AND;
    }

    public CompositeConditionBean(CompositeConditionBeanBuilder builder)
    {
        super(builder);

        if (null == conditions)
        {
            this.conditions = new ArrayList<ConditionalBean>();
        }

        if (null == type)
        {
            this.type = CompositeConditionType.AND;
        }
    }

    public List<ConditionalBean> getConditions()
    {
        return conditions;
    }

    public CompositeConditionType getType()
    {
        return type;
    }

    public static CompositeConditionBeanBuilder newCompositeConditionBean()
    {
        return new CompositeConditionBeanBuilder();
    }

    public static CompositeConditionBeanBuilder newCompositeConditionBean(CompositeConditionBean defaultBean)
    {
        return new CompositeConditionBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(conditions, type);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof CompositeConditionBean))
        {
            return false;
        }
        else
        {
            final CompositeConditionBean that = (CompositeConditionBean) obj;
            return Objects.equal(conditions, that.conditions) &&
                    Objects.equal(type, that.type);
        }
    }
}
