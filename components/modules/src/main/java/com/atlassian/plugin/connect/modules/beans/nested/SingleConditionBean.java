package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BeanWithParams;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.google.common.base.Objects;

/**
 * Single Conditions are either provided [by the host application](../../concepts/conditions.html#static) or
 * [by the add-on](../../concepts/conditions.html#remote). See the complete documentation of
 * [Conditions](../../concepts/conditions.html) for more information.
 *
 * To invert a condition, add the attribute ``invert="true"`` to the condition element.
 * This is useful where you want to show the section if a certain condition is not satisfied.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#SINGLE_CONDITION_EXAMPLE}
 * @schemaTitle Single Condition
 * @since 1.0
 */
@SchemaDefinition("singleCondition")
public class SingleConditionBean extends BeanWithParams implements ConditionalBean
{

    /**
     * A string indicating:
     *
     * <ul>
     *     <li>For static conditions: the name of the condition</li>
     *     <li>For remote conditions: the URL of the condition end-point exposed by the add-on</li>
     * </ul>
     */
    @Required
    private String condition;

    /**
     * A flag indicating whether to invert the boolean result of the condition.
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean invert;

    public SingleConditionBean()
    {
        this.condition = "";
        this.invert = false;
    }

    public SingleConditionBean(SingleConditionBeanBuilder builder)
    {
        super(builder);

        if (null == condition)
        {
            this.condition = "";
        }
        if (null == invert)
        {
            this.invert = false;
        }
    }

    public String getCondition()
    {
        return condition;
    }

    public Boolean isInvert()
    {
        return invert;
    }

    public static SingleConditionBeanBuilder newSingleConditionBean()
    {
        return new SingleConditionBeanBuilder();
    }

    public static SingleConditionBeanBuilder newSingleConditionBean(SingleConditionBean defaultBean)
    {
        return new SingleConditionBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(condition, invert);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof SingleConditionBean))
        {
            return false;
        }
        else
        {
            final SingleConditionBean that = (SingleConditionBean) obj;
            return Objects.equal(condition, that.condition) &&
                    Objects.equal(invert, that.invert);
        }
    }
}
