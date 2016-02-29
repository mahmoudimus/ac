package com.atlassian.plugin.connect.plugin.property;

import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;

public class AddonEntityPropertyEqualToConditionClassResolver implements ConnectConditionClassResolver {

    @Override
    @SuppressWarnings("unchecked")
    public List<Entry> getEntries() {
        Predicate<Map<String, String>> predicate = parameters -> "addon".equals(parameters.get("entity"));
        return Collections.singletonList(
                newEntry("entity_property_equal_to", AddonEntityPropertyEqualToCondition.class)
                        .contextFree()
                        .withPredicates(predicate)
                        .build()
        );
    }
}
