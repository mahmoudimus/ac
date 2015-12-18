package com.atlassian.plugin.connect.plugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper;

import com.google.common.collect.Sets;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reflections.Reflections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsMapContaining.hasKey;

/**
 * Checks whether the JSON whitelist entries have corresponding classes and fields.
 * Syntax errors and typos in the whitelist JSON will be caught.
 */
@RunWith(Parameterized.class)
public class AnalyticsWhitelistTest
{
    private static boolean isBrowserEvent(String eventName) throws IOException
    {
        return eventName.startsWith("connect.addon.iframe")
         || eventName.startsWith("connect.addon.bridge")
         || eventName.startsWith("connect.addon.dialog");
    }

    @Parameterized.Parameters(name = "Event {0}")
    public static Collection<Object[]> testData() throws IOException
    {
        String json = IOUtils.toString(ClassLoader.class.getResourceAsStream("/analytics/connect-analytics-whitelist.json"));
        Map<String, List<String>> whiteList = new Gson().fromJson(json, Map.class);

        Collection<Object[]> toTest = new ArrayList<Object[]>();
        for (Map.Entry<String, List<String>> entry : whiteList.entrySet())
        {
            if (!isBrowserEvent(entry.getKey()))
            {
                toTest.add(new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        return toTest;
    }

    @BeforeClass
    public static void collectEventClasses()
    {
        Set<Class<?>> union = reflectAllEvents("com.atlassian.plugin.connect.plugin", "com.atlassian.plugin.connect.confluence");
        for (Class<?> eventClass : union)
        {
            List<Field> fields = ConnectReflectionHelper.getAllFieldsInObjectChain(eventClass);
            List<String> fieldNames = fields.stream().map(Field::getName).collect(Collectors.toList());
            String eventName = eventClass.getAnnotation(EventName.class).value();
            eventClassFields.put(eventName, fieldNames);
        }
    }

    private static Set<Class<?>> reflectAllEvents(final String ... packages)
    {
        Set<Class<?>> clazzes = Sets.newHashSet();
        for (String p : packages)
        {
            Reflections coreReflection = new Reflections(p);
            clazzes.addAll(coreReflection.getTypesAnnotatedWith(EventName.class));
        }
        return clazzes;
    }

    private static Map<String, List<String>> eventClassFields = new HashMap<String, List<String>>();

    private final String eventName;
    private final List<String> whiteListedFields;

    public AnalyticsWhitelistTest(String eventName, List<String> whiteListedFields)
    {
        this.eventName = eventName;
        this.whiteListedFields = whiteListedFields;
    }

    @Test
    public void whiteListEventNameMatchesClassAnnotation()
    {
        assertThat(eventClassFields, hasKey(eventName));
    }

    @Test
    public void whiteListAttributesMatchFields()
    {
        // make hamcrest happy
        String[] jsonWhitelistFields = whiteListedFields.toArray(new String[whiteListedFields.size()]);
        assertThat(eventClassFields.get(eventName), hasItems(jsonWhitelistFields));
    }

}
