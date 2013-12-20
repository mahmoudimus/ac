package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.CompositeConditionBeanBuilder;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite Conditions are composed of a collection of Single Condition/Composite Conditions and a type attribute.
 *
 * The type attribute defines what logical operator is used to evaluate its collection of condition elements.
 *
 * The type can be one of "and" or "or"
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#COMPOSITE_CONDITION_EXAMPLE}
 * @schemaTitle Composite Condition
 * @since 1.0
 */
public class CompositeConditionBean extends BaseModuleBean implements ConditionalBean
{
    private List<ConditionalBean> conditions;
    private CompositeConditionType type;

    public CompositeConditionBean()
    {
        this.conditions = new ArrayList<ConditionalBean>();
        this.type = CompositeConditionType.AND;
    }

    public CompositeConditionBean(CompositeConditionBeanBuilder builder)
    {
        super(builder);

        if(null == conditions)
        {
            this.conditions = new ArrayList<ConditionalBean>();
        }

        if(null == type)
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
