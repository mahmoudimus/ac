package com.atlassian.plugin.connect.spi.web.condition;

import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.web.Condition;
import com.google.common.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 *
 * @see com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor
 */
public interface ConnectConditionClassResolver {

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

        private final String conditionName;

        private final Class<? extends Condition> conditionClass;

        private final List<Predicate<Map<String, String>>> parameterPredicates;

        private final boolean contextFree;

        private final boolean inlineSupport;

        private Entry(Builder builder) {
            this.conditionName = builder.conditionName;
            this.conditionClass = builder.conditionClass;
            this.parameterPredicates = builder.parameterPredicates;
            this.contextFree = builder.contextFree;
            this.inlineSupport = builder.inlineSupport;
        }

        @VisibleForTesting
        public String getConditionName() {
            return conditionName;
        }

        /**
         * Creates a builder for an entry with the given fields.
         *
         * @param conditionName the symbolic name of the condition
         * @param conditionClass the condition class
         * @return a resolver entry builder
         */
        public static Builder newEntry(String conditionName, Class<? extends Condition> conditionClass) {
            return new Builder(conditionName, conditionClass);
        }

        /**
         * Returns a condition class for use <b>where the full host application context is available</b>, if this entry
         * applies to the given condition element.
         *
         * @param conditionBean a condition element from an add-on descriptor
         * @return the condition class or {@link Optional#empty()}
         */
        public Optional<Class<? extends Condition>> getConditionClassForHostContext(SingleConditionBean conditionBean) {
            return getConditionClass(conditionBean, false, false);
        }

        /**
         * Returns a condition class for use <b>as an inline condition</b>, if this entry
         * applies to the given condition element.
         *
         * @param conditionBean a condition element from an add-on descriptor
         * @return the condition class or {@link Optional#empty()}
         */
        public Optional<Class<? extends Condition>> getConditionClassForInline(SingleConditionBean conditionBean) {
            return getConditionClass(conditionBean, false, true);
        }

        /**
         * Returns a condition class for use <b>where no context is available</b>, if this entry applies to the given
         * condition element.
         *
         * @param conditionBean a condition element from an add-on descriptor
         * @return the condition class or {@link Optional#empty()}
         */
        public Optional<Class<? extends Condition>> getConditionClassForNoContext(SingleConditionBean conditionBean) {
            return getConditionClass(conditionBean, true, false);
        }

        private Optional<Class<? extends Condition>> getConditionClass(SingleConditionBean conditionBean, boolean requireContextFree, boolean inline) {
            Optional<Class<? extends Condition>> optionalClass = Optional.empty();
            if (isApplicableToContext(requireContextFree)
                    && this.conditionName.equals(conditionBean.getCondition())
                    && isApplicableToParameters(conditionBean.getParams())
                    && (!inline || inlineSupport)) {
                optionalClass = Optional.of(conditionClass);
            }
            return optionalClass;
        }

        private boolean isApplicableToContext(boolean requireContextFree) {
            return !requireContextFree || contextFree;
        }

        private boolean isApplicableToParameters(Map<String, String> conditionParameters) {
            return parameterPredicates.stream()
                    .reduce(Predicate::and)
                    .map(predicate -> predicate.test(conditionParameters))
                    .orElse(true);
        }

        /**
         * A builder for condition class resolver entries.
         */
        public static final class Builder {

            private String conditionName;

            private Class<? extends Condition> conditionClass;

            private List<Predicate<Map<String, String>>> parameterPredicates = Collections.emptyList();

            private boolean contextFree;
            public boolean inlineSupport = true;

            private Builder(String conditionName, Class<? extends Condition> conditionClass) {
                this.conditionName = conditionName;
                this.conditionClass = conditionClass;
            }

            /**
             * Adds the given parameter predicates to the builder. The predicates must be satisfied for the entry
             * to be used.
             *
             * @param parameterPredicates predicates on the parameters of the condition
             * @return the builder
             */
            @SafeVarargs
            public final Builder withPredicates(Predicate<Map<String, String>>... parameterPredicates) {
                this.parameterPredicates = Arrays.asList(parameterPredicates);
                return this;
            }

            /**
             * Marks the condition as being usable also where the full host application context is not available.
             * Connect may evaluate this condition even when the context map is not fully populated.
             * Conditions marked with this flag should generally not access any fields from the context map.
             *
             * @return the builder
             */
            public Builder contextFree() {
                this.contextFree = true;
                return this;
            }

            /**
             * Some conditions may use ephemeral context variables which cannot be recreated
             * at the time of URL variable substitution. Mark such conditions using this method.
             * This will cause them not be resolved at all, in other words they will not be available
             * in context parameters.
             *
             * @return this builder
             */
            public Builder withoutInlineSupport() {
                this.inlineSupport = false;
                return this;
            }

            /**
             * Builds the resolver entry.
             *
             * @return the resolver entry
             */
            public Entry build() {
                return new Entry(this);
            }
        }
    }
}
