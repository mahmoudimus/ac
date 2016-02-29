package com.atlassian.plugin.connect.confluence.analytics;

import com.atlassian.plugin.connect.test.AnalyticsWhitelistTestHelper;
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
 *
 */
@RunWith(Parameterized.class)
public class ConfluenceAnalyticsWhitelistTest {
    private static Map<String, List<String>> eventClassFields = new HashMap<>();

    private final String eventName;
    private final List<String> whiteListedFields;

    public ConfluenceAnalyticsWhitelistTest(String eventName, List<String> whiteListedFields) {
        this.eventName = eventName;
        this.whiteListedFields = whiteListedFields;
    }

    @Parameterized.Parameters(name = "Event {0}")
    public static Collection<Object[]> testData() throws IOException {
        Map<String, List<String>> whiteList = AnalyticsWhitelistTestHelper.getAnalyticsWhitelistFrom("/analytics/confluence-analytics-whitelist.json");
        Collection<Object[]> toTest = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : whiteList.entrySet()) {
            toTest.add(new Object[]{entry.getKey(), entry.getValue()});
        }
        return toTest;
    }

    @BeforeClass
    public static void collectEventClasses() {
        eventClassFields.putAll(AnalyticsWhitelistTestHelper.reflectAllEventClassesFrom("com.atlassian.plugin.connect.confluence"));
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
