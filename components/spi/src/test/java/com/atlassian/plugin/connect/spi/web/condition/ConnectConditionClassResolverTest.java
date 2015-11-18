package com.atlassian.plugin.connect.spi.web.condition;

import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.web.Condition;
import org.junit.Test;

import java.util.Optional;

import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConnectConditionClassResolverTest
{

    private static final String CONDITION_NAME = "some-condition";

    private static final Class<Condition> CONDITION_CLASS = Condition.class;

    @Test
    public void shouldReturnConditionClassForHostContextForEntry()
    {
        ConnectConditionClassResolver.Entry entry = newEntry(CONDITION_NAME, CONDITION_CLASS).build();
        assertThat(entry.getConditionClassForHostContext(newCondition(CONDITION_NAME)), equalTo(Optional.of(CONDITION_CLASS)));
    }

    @Test
    public void shouldReturnNothingForNoContextForEntry()
    {
        ConnectConditionClassResolver.Entry entry = newEntry(CONDITION_NAME, CONDITION_CLASS).build();
        assertThat(entry.getConditionClassForNoContext(newCondition(CONDITION_NAME)), equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnConditionClassForNoContextForContextFreeEntry()
    {
        ConnectConditionClassResolver.Entry entry = newEntry(CONDITION_NAME, CONDITION_CLASS).contextFree().build();
        assertThat(entry.getConditionClassForNoContext(newCondition(CONDITION_NAME)), equalTo(Optional.of(CONDITION_CLASS)));
    }

    @Test
    public void shouldReturnNothingForEntryWithUnsatisfiedPredicate()
    {
        ConnectConditionClassResolver.Entry entry = newEntry(CONDITION_NAME, CONDITION_CLASS).withPredicates((parameters) -> false).build();
        assertThat(entry.getConditionClassForHostContext(newCondition(CONDITION_NAME)), equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnConditionClassForEntryWithSatisfiedPredicate()
    {
        SingleConditionBean conditionBean = newSingleConditionBean().withCondition(CONDITION_NAME).withParam("some-parameter", "some-value").build();
        ConnectConditionClassResolver.Entry entry = newEntry(CONDITION_NAME, CONDITION_CLASS)
                .withPredicates((parameters) -> !parameters.isEmpty()).build();
        assertThat(entry.getConditionClassForHostContext(conditionBean), equalTo(Optional.of(Condition.class)));
    }

    private SingleConditionBean newCondition(String condition)
    {
        return newSingleConditionBean().withCondition(condition).build();
    }
}
