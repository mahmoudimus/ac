package com.atlassian.plugin.connect.spi.web.condition;

import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.web.Condition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A plugin module descriptor interface for providers of conditions for Atlassian Connect.
 *
 * A condition class resolver provides class resolution for symbolic condition names used in the nested
 * <pre>conditions</pre> field of some modules in the Atlassian Connect add-on JSON descriptor.
 *
 * Resolvers can optionally provide one or more predicates to evaluate for the parameters provided to the condition
 * element.
 *
 * <b>NOTE:</b> the {@link com.atlassian.plugin.web.WebFragmentHelper} of the host products generally auto-wires
 * conditions in the context of the plugin that registers a plugin module making use of it.
 */
public interface ConnectConditionClassResolver
{

    /**
     * Returns the list of entries provided by this condition class resolver.
     *
     * @return a list of entries
     */
    List<Entry> getEntries();

    /**
     * A condition class resolver entry, representing a mapping from symbolic condition name to condition class.
     */
    final class Entry {

        private String conditionName;

        private Class<? extends Condition> conditionClass;

        private List<Predicate<Map<String, String>>> parameterPredicates;

        private Entry(String conditionName, Class<? extends Condition> conditionClass, Predicate<Map<String, String>>... parameterPredicates)
        {
            this.conditionName = conditionName;
            this.conditionClass = conditionClass;
            this.parameterPredicates = Arrays.asList(parameterPredicates);
        }

        /**
         * Creates an entry with the given fields.
         *
         * @param conditionName the symbolic name of the condition
         * @param conditionClass the condition class
         * @param parameterPredicates predicates on the parameters of the condition that must be satisfied for the entry to be used
         * @return the created resolver entry
         */
        public static Entry newEntry(String conditionName, Class<? extends Condition> conditionClass, Predicate<Map<String, String>>... parameterPredicates)
        {
            return new Entry(conditionName, conditionClass, parameterPredicates);
        }

        /**
         * Returns the condition class, if this entry applies to the given condition element.
         *
         * @param conditionBean a condition element from an add-on descriptor
         * @return the condition class or {@link Optional#empty()}
         */
        public Optional<Class<? extends Condition>> getConditionClass(SingleConditionBean conditionBean)
        {

            Optional<Class<? extends Condition>> optionalClass = Optional.empty();
            if (this.conditionName.equals(conditionBean.getCondition()) && isApplicable(conditionBean.getParams()))
            {
                optionalClass = Optional.of(conditionClass);
            }
            return optionalClass;
        }

        private boolean isApplicable(Map<String, String> conditionParameters)
        {
            return parameterPredicates.stream()
                    .reduce(new BinaryOperator<Predicate<Map<String, String>>>()
                    {
                        @Override
                        public Predicate<Map<String, String>> apply(Predicate<Map<String, String>> p1, Predicate<Map<String, String>> p2)
                        {
                            return p1.and(p2);
                        }
                    })
                    .map(new Function<Predicate<Map<String, String>>, Boolean>()
                    {
                        @Override
                        public Boolean apply(Predicate<Map<String, String>> predicate)
                        {
                            return predicate.test(conditionParameters);
                        }
                    })
                    .orElse(true);
        }
    }
}
