package com.atlassian.plugin.connect.api.web.condition;

import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.web.Condition;

import java.util.Optional;

/**
 * A service providing access to web fragment condition class mappings for Atlassian Connect conditions.
 *
 * @see Condition
 */
public interface ConditionClassAccessor
{

    /**
     * Returns a condition class for use <b>where the full host application context is available</b>, if this entry
     * applies to the given condition element.
     *
     * @param conditionBean a condition element from an add-on descriptor
     * @return the condition class or {@link Optional#empty()}
     */
    public Optional<Class<? extends Condition>> getConditionClassForHostContext(SingleConditionBean conditionBean);

    /**
     * Returns a condition class for use <b>where no context is available</b>, if this entry applies to the given
     * condition element.
     *
     * @param conditionBean a condition element from an add-on descriptor
     * @return the condition class or {@link Optional#empty()}
     */
    public Optional<Class<? extends Condition>> getConditionClassForNoContext(SingleConditionBean conditionBean);
}
