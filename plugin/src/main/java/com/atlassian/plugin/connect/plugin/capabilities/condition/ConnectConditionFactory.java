package com.atlassian.plugin.connect.plugin.capabilities.condition;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.web.Condition;

import java.util.List;

/**
 * Bridges Atlassian Connect's {@link ConditionalBean} and Atlassian Plugins 2's {@link Condition} classes.
 *
 * @since 1.0
 */
public interface ConnectConditionFactory
{
    /**
     * @return a {@link Condition} that returns true iff the logical tests defined by the supplied list of
     * {@link ConditionalBean} evaluate to {@code TRUE}.
     */
    Condition createCondition(String addOnKey, List<ConditionalBean> conditionalBeans);

    /**
     * @return a {@link Condition} that returns true iff the logical tests defined by the supplied list of
     * {@link ConditionalBean} evaluate to {@code TRUE} AND the specified plugins2 {@link Condition}
     * class also evaluates to {@code TRUE}.
     */
    Condition createCondition(String addOnKey, List<ConditionalBean> conditionalBeans, Class<? extends Condition> additionalConditions);

    /**
     * @return a {@link Condition} that returns true iff the logical tests defined by the supplied list of
     * {@link ConditionalBean} evaluate to {@code TRUE} AND instances of the specified plugins2 {@link Condition}
     * classes also evaluate to {@code TRUE}.
     */
    Condition createCondition(String addOnKey, List<ConditionalBean> conditionalBeans, Iterable<Class<? extends Condition>> additionalConditions);
}
