package com.atlassian.plugin.connect.plugin.web.condition;

import java.util.Collections;
import java.util.List;

import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;

import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;

public class LicensedConditionClassResolver implements ConnectConditionClassResolver
{
    @Override
    public List<Entry> getEntries()
    {
        return Collections.singletonList(
            newEntry("addon_is_licensed", IsLicensedCondition.class).contextFree().build()
        );
    }
}
