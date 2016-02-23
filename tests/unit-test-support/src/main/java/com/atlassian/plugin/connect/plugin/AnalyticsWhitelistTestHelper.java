package com.atlassian.plugin.connect.plugin;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;

import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class to gather analytic events reflectively for testing purposes.
 */
public final class AnalyticsWhitelistTestHelper {
    private static boolean isBrowserEvent(String eventName) {
        return eventName.startsWith("connect.addon.iframe")
                || eventName.startsWith("connect.addon.bridge")
                || eventName.startsWith("connect.addon.dialog");
    }

    /**
     * given a class path to an analytics whitelist json file, return a map of the name of the event, and the list
     * of whitelisted properties associated with that event. Filters out the browser based events.
     * @param path a path to the whitelist json file
     * @return a map of the name of the event to the properties of that event
     * @throws IOException if the whitelist json file given isn't in the current classpath
     */
    public static Map<String, List<String>> getAnalyticsWhitelistFrom(final String path) throws IOException {
        String json = IOUtils.toString(ClassLoader.class.getResourceAsStream(path));
        Map<String, List<String>> result = new Gson().fromJson(json, new TypeToken<Map<String, List<String>>>() {
        }.getType());
        return Maps.filterEntries(result, entry -> !isBrowserEvent(entry.getKey()));
    }

    /**
     * Given a full package name, return a map of the name of the events in that package, and the list of fields in each of the
     * events found.
     * @param packageName a full package name
     * @return a map of all of the events found in the package, keyed by the name, and the list of fields for each event.
     */
    public static Map<String, List<String>> reflectAllEventClassesFrom(final String packageName) {
        Map<String, List<String>> result = Maps.newHashMap();
        Set<Class<?>> union = reflectAllEvents(packageName);
        for (Class<?> eventClass : union) {
            List<String> fieldNames = ConnectReflectionHelper.getAllGettersInObjectChain(eventClass)
                    .stream()
                    .map(Member::getName)
                    .map(getter -> getter.replaceFirst("^get", "")) // "getSomeName" -> "SomeName"
                    .map(Introspector::decapitalize) // "SomeName" -> "someName"
                    .collect(Collectors.toList());
            String eventName = eventClass.getAnnotation(EventName.class).value();

            result.put(eventName, fieldNames);
        }
        return result;
    }

    private static Set<Class<?>> reflectAllEvents(final String... packages) {
        Set<Class<?>> clazzes = Sets.newHashSet();
        for (String p : packages) {
            Reflections coreReflection = new Reflections(p);
            clazzes.addAll(coreReflection.getTypesAnnotatedWith(EventName.class));
        }
        return clazzes;
    }
}
