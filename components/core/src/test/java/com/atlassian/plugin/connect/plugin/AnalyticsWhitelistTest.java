package com.atlassian.plugin.connect.plugin;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.lang.reflect.Field;
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
        String json = IOUtils.toString(ClassLoader.class.getResourceAsStream("/whitelist/connect_whitelist.json"));
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
    public static void collectEventClasses() throws ClassNotFoundException
    {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(EventName.class));

        String[] eventPackages = new String[] {
                "com.atlassian.plugin.connect.plugin.auth.scope",
                "com.atlassian.plugin.connect.plugin.lifecycle.analytics",
                "com.atlassian.plugin.connect.plugin.web.condition"
        };
        for (String eventPackage : eventPackages)
        {
            for (BeanDefinition bd : scanner.findCandidateComponents(eventPackage))
            {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                EventName en = clazz.getAnnotation(EventName.class);

                List<String> fieldNames = new ArrayList<String>();
                for (Field field : ConnectReflectionHelper.getAllFieldsInObjectChain(clazz))
                {
                    fieldNames.add(field.getName());
                }

                eventClassFields.put(en.value(), fieldNames);
            }
        }
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