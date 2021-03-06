package com.atlassian.plugin.connect.test.matcher;

import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AbstractCompositeCondition;
import org.hamcrest.Description;
import org.hamcrest.core.IsCollectionContaining;
import org.mockito.ArgumentMatcher;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class ConditionMatchers {
    public static ArgumentMatcher<Condition> isCompositeConditionContaining(
            final Class<? extends AbstractCompositeCondition> expectedCompositeConditionType,
            final Condition... expectedNestedConditions) {
        return new ArgumentMatcher<Condition>() {
            @Override
            public boolean matches(Object argument) {
                assertThat(argument, is(instanceOf(expectedCompositeConditionType)));
                List<Condition> conditionList = getNestedConditionsReflectively((AbstractCompositeCondition) argument);
                assertThat(conditionList, IsCollectionContaining.hasItems(expectedNestedConditions));
                assertThat(conditionList.size(), is(equalTo(expectedNestedConditions.length)));
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Condition is ")
                        .appendText(expectedCompositeConditionType.getSimpleName())
                        .appendText(" with nested conditions ")
                        .appendText(Arrays.toString(expectedNestedConditions));
            }
        };
    }

    public static ArgumentMatcher<Condition> isCompositeConditionContainingSimpleName(
            final Class<? extends AbstractCompositeCondition> expectedCompositeConditionType,
            final String... expectedNestedConditionNames) {
        return new ArgumentMatcher<Condition>() {
            @Override
            public boolean matches(Object argument) {
                assertThat(argument, is(instanceOf(expectedCompositeConditionType)));
                List<String> conditionList = getNestedConditionSimpleNamesReflectively((AbstractCompositeCondition) argument);
                assertThat(conditionList, IsCollectionContaining.hasItems(expectedNestedConditionNames));
                assertThat(conditionList.size(), is(equalTo(expectedNestedConditionNames.length)));
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Condition is ")
                        .appendText(expectedCompositeConditionType.getSimpleName())
                        .appendText(" with nested conditions ")
                        .appendText(Arrays.toString(expectedNestedConditionNames));
            }
        };
    }

    private static List<Condition> getNestedConditionsReflectively(final AbstractCompositeCondition condition) {
        try {
            Field conditions = AbstractCompositeCondition.class.getDeclaredField("conditions");
            conditions.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Condition> list = (List<Condition>) conditions.get(condition);
            return list;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(condition.getClass().getSimpleName() + " no longer has a field named 'conditions'", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("The 'conditions' field is no longer a List in " + condition.getClass().getSimpleName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> getNestedConditionSimpleNamesReflectively(final AbstractCompositeCondition condition) {
        try {
            Field conditions = AbstractCompositeCondition.class.getDeclaredField("conditions");
            conditions.setAccessible(true);
            return ((List<Condition>) conditions.get(condition)).stream()
                    .map(nested -> nested.getClass().getSimpleName())
                    .collect(Collectors.toList());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(condition.getClass().getSimpleName() + " no longer has a field named 'conditions'", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("The 'conditions' field is no longer a List in " + condition.getClass().getSimpleName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
