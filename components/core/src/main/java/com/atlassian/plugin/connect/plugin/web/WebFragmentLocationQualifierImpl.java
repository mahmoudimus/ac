package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.connect.api.web.WebFragmentLocationQualifier;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.atlassian.plugin.connect.plugin.descriptor.ModuleValidationExceptionHandler;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class WebFragmentLocationQualifierImpl implements WebFragmentLocationQualifier
{

    private static final char LOCATION_SEGMENT_SEPARATOR = '/';

    private Joiner segmentJoiner = Joiner.on(LOCATION_SEGMENT_SEPARATOR);
    private Splitter segmentSplitter = Splitter.on(LOCATION_SEGMENT_SEPARATOR);

    /**
     * A map of unqualified key -> qualified key for all modules that can be referenced from the locations of other
     * modules.
     */
    private Map<ConnectAddonBean, Map<String, String>> keyMapCache = new WeakHashMap<>();

    @Override
    public String processLocation(String location, ConnectAddonBean addon)
    {
        Iterable<String> segments = segmentSplitter.split(location);

        final Iterable<String> processedSegments = Iterables.transform(segments, segment -> processSegment(segment, addon));

        return segmentJoiner.join(processedSegments);
    }

    private String processSegment(String location, ConnectAddonBean addon)
    {
        Map<String, String> keyMap = keyMapCache.computeIfAbsent(addon, WebFragmentLocationQualifierImpl::buildKeyMap);

        return keyMap.getOrDefault(location, location);
    }

    private static ImmutableMap<String, String> buildKeyMap(ConnectAddonBean addon)
    {
        ImmutableMap.Builder<String, String> keyMapBuilder = ImmutableMap.<String, String>builder();
        Consumer<Exception> moduleValidationExceptionHandler = new ModuleValidationExceptionHandler();

        {
            Optional<List<ModuleBean>> optionalWebItems = addon.getModules().getValidModuleListOfType("webItems", moduleValidationExceptionHandler);
            optionalWebItems.ifPresent(
                webItems -> keyMapBuilder.putAll(createKeyToQualifiedKeyMap(addon, webItems))
            );
        }

        {
            Optional<List<ModuleBean>> optionalWebSections = addon.getModules().getValidModuleListOfType("webSections", moduleValidationExceptionHandler);
            optionalWebSections.ifPresent(
                webSections -> keyMapBuilder.putAll(createKeyToQualifiedKeyMap(addon, webSections))
            );
        }

        return keyMapBuilder.build();
    }

    private static Map<String, String> createKeyToQualifiedKeyMap(ConnectAddonBean addon, List<ModuleBean> modules)
    {
        List<RequiredKeyBean> requiredKeyBeans = modules.stream()
                .filter(module -> module instanceof RequiredKeyBean)
                .map(RequiredKeyBean.class::cast)
                .collect(Collectors.toList());

        ImmutableMap<String, RequiredKeyBean> rawKeyMap = Maps.uniqueIndex(requiredKeyBeans, RequiredKeyBean::getRawKey);

        return Maps.transformValues(rawKeyMap, requiredKeyBean -> requiredKeyBean.getKey(addon));
    }
}
