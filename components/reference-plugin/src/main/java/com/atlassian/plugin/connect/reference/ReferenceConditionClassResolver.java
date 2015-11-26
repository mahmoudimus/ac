package com.atlassian.plugin.connect.reference;

import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.conditions.NeverDisplayCondition;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;

public class ReferenceConditionClassResolver implements ConnectConditionClassResolver
{

    @Override
    public List<Entry> getEntries()
    {
        return ImmutableList.of(
                newEntry("always-display", AlwaysDisplayCondition.class).build(),
                newEntry("never-display", NeverDisplayCondition.class).build()
        );
    }
}
