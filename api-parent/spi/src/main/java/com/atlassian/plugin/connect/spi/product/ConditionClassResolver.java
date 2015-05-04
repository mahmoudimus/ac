package com.atlassian.plugin.connect.spi.product;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class is used to map connect conditions/parameters pairs to actual condition classes.
 *
 * <p>
 *     Instances of this class contain simple mapping between condition names and condition classes but also a
 *     more sophisticated rules that can assign condition classes based on condition names and parameters.
 * </p>
 *
 * <p>
 *     Rules are resolved first and if none match, then mapping is considered.
 * </p>
 */
public final class ConditionClassResolver
{
    private final Map<String, Class<? extends Condition>> conditionsMap;
    private final List<Rule> conditionRules;
    private final Set<String> allConditionNames;

    private ConditionClassResolver(final Map<String, Class<? extends Condition>> conditionsMap, final List<Rule> conditionRules, final Collection<String> allConditionNames)
    {
        this.allConditionNames = new TreeSet<>(allConditionNames);
        this.conditionsMap = new TreeMap<>(conditionsMap);
        this.conditionRules = ImmutableList.copyOf(conditionRules);
    }

    public Option<? extends Class<? extends Condition>> get(final String condition, final Map<String, String> params)
    {
        for (Rule rule : conditionRules)
        {
            if (rule.matches(condition, params))
            {
                return Option.some(rule.getConditionClass());
            }
        }
        return Option.option(conditionsMap.get(condition));
    }

    public Set<String> getAllConditionNames()
    {
        return allConditionNames;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private final Map<String, Class<? extends Condition>> conditionsMap = Maps.newTreeMap();
        private final List<Rule> conditionRules = Lists.newArrayList();
        private final Set<String> allConditionNames = Sets.newTreeSet();

        public Builder() {}

        public Builder with(ConditionClassResolver resolver)
        {
            conditionsMap.putAll(resolver.conditionsMap);
            conditionRules.addAll(resolver.conditionRules);
            allConditionNames.addAll(resolver.allConditionNames);
            return this;
        }

        public Builder mapping(String conditionName, Class<? extends Condition> conditionClass)
        {
            this.conditionsMap.put(conditionName, conditionClass);
            this.allConditionNames.add(conditionName);
            return this;
        }

        public Builder rule(final String conditionName, final Predicate<Map<String, String>> rulePredicate, Class<? extends Condition> condition)
        {
            conditionRules.add(new Rule(conditionName, rulePredicate, condition));
            allConditionNames.add(conditionName);
            return this;
        }

        public ConditionClassResolver build()
        {
            return new ConditionClassResolver(conditionsMap, conditionRules, allConditionNames);
        }
    }

    private static class Rule
    {
        private final String conditionName;
        private final Predicate<Map<String, String>> paramsPredicate;
        private final Class<? extends Condition> conditionClass;

        public Rule(final String conditionName, final Predicate<Map<String, String>> paramsPredicate, final Class<? extends Condition> conditionClass)
        {
            this.conditionName = checkNotNull(conditionName);
            this.paramsPredicate = checkNotNull(paramsPredicate);
            this.conditionClass = checkNotNull(conditionClass);
        }

        final boolean matches(String conditionName, Map<String, String> parameters)
        {
            return this.conditionName.equals(conditionClass) && paramsPredicate.apply(parameters);
        }

        public Class<? extends Condition> getConditionClass()
        {
            return conditionClass;
        }
    }
}
