package com.atlassian.plugin.connect.spi.product;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
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
    private final List<Pair<Rule, Class<? extends Condition>>> conditionRules;
    private final Set<String> allConditionNames;

    private ConditionClassResolver(final Map<String, Class<? extends Condition>> conditionsMap, final List<Pair<Rule, Class<? extends Condition>>> conditionRules, final Collection<String> allConditionNames)
    {
        this.allConditionNames = new TreeSet<>(allConditionNames);
        this.conditionsMap = new TreeMap<>(conditionsMap);
        this.conditionRules = ImmutableList.copyOf(conditionRules);
    }

    public Option<? extends Class<? extends Condition>> get(final String condition, final Map<String, String> params)
    {
        for (Pair<Rule, Class<? extends Condition>> rule : conditionRules)
        {
            if (rule.left().matches(condition, params))
            {
                return Option.some(rule.right());
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
        private final List<Pair<Rule, Class<? extends Condition>>> conditionRules = Lists.newArrayList();
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
            Rule rule = new Rule()
            {
                @Override
                public boolean matches(final String condition, final Map<String, String> parameters)
                {
                    return conditionName.equals(condition) && rulePredicate.apply(parameters);
                }
            };
            conditionRules.add(Pair.<Rule, Class<? extends Condition>>pair(rule, condition));
            allConditionNames.add(conditionName);
            return this;
        }

        public ConditionClassResolver build()
        {
            return new ConditionClassResolver(conditionsMap, conditionRules, allConditionNames);
        }
    }

    private interface Rule
    {
        boolean matches(String conditionName, Map<String, String> parameters);
    }
}
