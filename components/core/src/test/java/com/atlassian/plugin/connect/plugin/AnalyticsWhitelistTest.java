package com.atlassian.plugin.connect.plugin;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsMapContaining.hasKey;

/**
 * Checks whether the JSON whitelist entries have corresponding classes and fields.
 * Syntax errors and typos in the whitelist JSON will be caught.
 */
@RunWith(Parameterized.class)
public class AnalyticsWhitelistTest {
    private static Map<String, List<String>> eventClassFields = new HashMap<>();

    private final String eventName;
    private final List<String> whiteListedFields;

    public AnalyticsWhitelistTest(String eventName, List<String> whiteListedFields) {
        this.eventName = eventName;
        this.whiteListedFields = whiteListedFields;
    }

    @Parameterized.Parameters(name = "Event {0}")
    public static Collection<Object[]> testData() throws IOException {
        Map<String, List<String>> whiteList = AnalyticsWhitelistTestHelper.getAnalyticsWhitelistFrom("/analytics/connect-analytics-whitelist.json");
        Collection<Object[]> toTest = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : whiteList.entrySet()) {
            toTest.add(new Object[]{entry.getKey(), entry.getValue()});
        }
        return toTest;
    }

    @BeforeClass
    public static void collectEventClasses() {
        eventClassFields.putAll(AnalyticsWhitelistTestHelper.reflectAllEventClassesFrom("com.atlassian.plugin.connect.plugin"));
    }

    @Test
    public void whiteListEventNameMatchesClassAnnotation() {
        assertThat(eventClassFields, hasKey(eventName));
    }

    @Test
    public void whiteListAttributesMatchFields() {
        // make hamcrest happy
        String[] jsonWhitelistFields = whiteListedFields.toArray(new String[whiteListedFields.size()]);
        assertThat(eventClassFields.get(eventName), hasItems(jsonWhitelistFields));
    }
}
