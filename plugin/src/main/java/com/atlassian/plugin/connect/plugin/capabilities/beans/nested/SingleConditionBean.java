package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BeanWithParams;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.SingleConditionBeanBuilder;
import com.google.common.base.Objects;

/**
 * Conditions can be added to modules to display them only when all the given conditions are true.
 *
 * Single Conditions can take optional parameters.
 * These parameters will be passed in to the condition's init() method as a map of string key/value pairs before any condition checks are performed.
 *
 * To invert a condition, add the attribute ``invert="true"`` to the condition element.
 * This is useful where you want to show the section if a certain condition is not satisfied.
 *
 * Single Conditions must contain a *condition* attribute with the name of the condition to check.
 *
 * The valid condition names are as follows:
 *
 *#### JIRA
 * {@see com.atlassian.plugin.connect.plugin.capabilities.beans.JiraConditions#CONDITION_LIST}
 *
 *
 *
 *#### CONFLUENCE
 * {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConfluenceConditions#CONDITION_LIST}
 *
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#SINGLE_CONDITION_EXAMPLE}
 * @schemaTitle Single Condition
 * @since 1.0
 * @schemaTitle Single Condition
 */
public class SingleConditionBean extends BeanWithParams implements ConditionalBean
{
    @Required
    private String condition;

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
