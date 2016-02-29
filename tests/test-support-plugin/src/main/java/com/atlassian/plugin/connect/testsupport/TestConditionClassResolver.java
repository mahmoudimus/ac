package com.atlassian.plugin.connect.testsupport;

import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.web.conditions.NeverDisplayCondition;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;

public class TestConditionClassResolver implements ConnectConditionClassResolver {

    @Override
    public List<Entry> getEntries() {
        return ImmutableList.of(
                newEntry("fail-on-instantiation", NonInstantiableCondition.class).build()
        );
    }

    private static class NonInstantiableCondition extends NeverDisplayCondition {
        public NonInstantiableCondition() {
            throw new RuntimeException("Failing on purpose");
        }
    }
}
