package com.atlassian.plugin.connect.plugin.property;

import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;

public class AddonEntityPropertyEqualToConditionClassResolver implements ConnectConditionClassResolver
{

    @Override
    public List<Entry> getEntries()
    {
        return Collections.singletonList(
                newEntry("entity_property_equal_to", AddonEntityPropertyEqualToCondition.class)
                        .contextFree()
                        .withPredicates(
                                new Predicate<Map<String, String>>()
                                {
                                    @Override
                                    public boolean test(Map<String, String> parameters)
                                    {
                                        return "addon".equals(parameters.get("entity"));
                                    }
                                })
                        .build()
        );
    }
}
