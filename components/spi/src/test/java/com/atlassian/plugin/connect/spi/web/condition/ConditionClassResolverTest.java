package com.atlassian.plugin.connect.spi.web.condition;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.web.condition.ConditionClassResolver;
import com.atlassian.plugin.connect.api.web.condition.UserIsAdminCondition;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.sal.api.features.DarkFeatureEnabledCondition;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class ConditionClassResolverTest
{

    @Test
    public void noneIsReturnedWhenResolverIsEmpty()
    {
        assertThat(resolverBuilder().build().get("a", emptyParameters()), equalToNone());
    }

    @Test
    public void mappedConditionIsReturned()
    {
        ConditionClassResolver resolver = resolverBuilder()
                .mapping("a", UserIsAdminCondition.class)
                .build();

        assertThat(resolver.get("a", emptyParameters()), equalToCondition(UserIsAdminCondition.class));
    }

    @Test
    public void mappingsAndRulesAreMerged()
    {
        ConditionClassResolver resolver = resolverBuilder()
                .mapping("a", UserIsAdminCondition.class)
                .rule("c", mappingExpectingParameter("c"), Condition.class)
                .build();

        ConditionClassResolver resolver2 = resolverBuilder()
                .with(resolver)
                .mapping("b", DarkFeatureEnabledCondition.class)
                .rule("d", mappingExpectingParameter("d"), AlwaysDisplayCondition.class)
                .build();

        assertThat(resolver2.get("a", emptyParameters()), equalToCondition(UserIsAdminCondition.class));
        assertThat(resolver2.get("b", emptyParameters()), equalToCondition(DarkFeatureEnabledCondition.class));
        assertThat(resolver2.get("c", ImmutableMap.of("c", "c")), equalToCondition(Condition.class));
        assertThat(resolver2.get("d", ImmutableMap.of("d", "d")), equalToCondition(AlwaysDisplayCondition.class));
    }

    private Predicate<Map<String, String>> mappingExpectingParameter(final String paramName)
    {
        return new Predicate<Map<String, String>>()
        {
            @Override
            public boolean apply(final Map<String, String> input)
            {
                return input.containsKey(paramName);
            }
        };
    }

    @Test
    public void mappingsAreMerged()
    {
        ConditionClassResolver resolver = resolverBuilder()
                .mapping("a", UserIsAdminCondition.class)
                .build();

        ConditionClassResolver resolver2 = resolverBuilder()
                .with(resolver)
                .mapping("b", DarkFeatureEnabledCondition.class)
                .build();

        assertThat(resolver2.get("a", emptyParameters()), equalToCondition(UserIsAdminCondition.class));
        assertThat(resolver2.get("b", emptyParameters()), equalToCondition(DarkFeatureEnabledCondition.class));
    }

    @Test
    public void addingTheSameMappingOverwritesTheExistingOne()
    {
        ConditionClassResolver resolver = resolverBuilder()
                .mapping("a", UserIsAdminCondition.class)
                .mapping("a", DarkFeatureEnabledCondition.class)
                .build();

        assertThat(resolver.get("a", emptyParameters()), equalToCondition(DarkFeatureEnabledCondition.class));
    }

    @Test
    public void mergingTheSameMappingOverwritesTheExistingOne()
    {
        ConditionClassResolver resolver = resolverBuilder()
                .mapping("a", UserIsAdminCondition.class)
                .build();

        ConditionClassResolver resolver2 = resolverBuilder()
                .mapping("a", DarkFeatureEnabledCondition.class)
                .with(resolver)
                .build();

        assertThat(resolver2.get("a", emptyParameters()), equalToCondition(UserIsAdminCondition.class));
    }

    @Test
    public void rulesTakePrecedenceBeforeMappings()
    {
        ConditionClassResolver resolver = resolverBuilder()
                .mapping("a", UserIsAdminCondition.class)
                .rule("a", new Predicate<Map<String, String>>()
                {
                    @Override
                    public boolean apply(final Map<String, String> input)
                    {
                        return Boolean.valueOf(input.get("match"));
                    }
                }, DarkFeatureEnabledCondition.class)
                .build();

        assertThat(resolver.get("a", ImmutableMap.of("match", "true")), equalToCondition(DarkFeatureEnabledCondition.class));
        assertThat(resolver.get("a", ImmutableMap.of("match", "false")), equalToCondition(UserIsAdminCondition.class));
    }

    @Test
    public void rulesAreInvokedOnlyWhenConditionNameMatches()
    {
        ConditionClassResolver resolver = resolverBuilder()
                .rule("a", new Predicate<Map<String, String>>()
                {
                    @Override
                    public boolean apply(final Map<String, String> input)
                    {
                        fail("this code should not have been invoked");
                        return true;
                    }
                }, DarkFeatureEnabledCondition.class)
                .build();

        assertThat(resolver.get("b", emptyParameters()), equalToNone());
    }

    @Test
    public void allConditionNamesAreGatheredInAlphabeticalOrder()
    {
        ConditionClassResolver resolver = resolverBuilder()
                .mapping("b", UserIsAdminCondition.class)
                .rule("a", mock(Predicate.class), DarkFeatureEnabledCondition.class)
                .rule("d", mock(Predicate.class), DarkFeatureEnabledCondition.class)
                .mapping("c", UserIsAdminCondition.class)
                .build();

        assertThat(Lists.newArrayList(resolver.getAllConditionNames()), equalTo(Lists.newArrayList("a", "b", "c", "d")));
    }

    private Matcher equalToNone()
    {
        return equalTo(Option.<Class>none());
    }

    private Matcher equalToCondition(Class conditionClass)
    {
        return equalTo(Option.some(conditionClass));
    }

    private static ConditionClassResolver.Builder resolverBuilder()
    {
        return ConditionClassResolver.builder();
    }

    private static Map<String, String> emptyParameters()
    {
        return Collections.emptyMap();
    }
}
