package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.connect.api.web.WebFragmentLocationQualifier;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.atlassian.plugin.connect.plugin.descriptor.LoggingModuleValidationExceptionHandler;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
        final Iterable<String> processedSegments = Iterables.transform(segments,
                new com.google.common.base.Function<String, String>()
                {
                    @Override
                    public String apply(@Nullable String segment)
                    {
                        return processSegment(segment, addon);
                    }
                });

        return segmentJoiner.join(processedSegments);
    }

    private String processSegment(String location, ConnectAddonBean addon)
    {
        Map<String, String> keyMap = keyMapCache.computeIfAbsent(addon, new Function<ConnectAddonBean, Map<String, String>>()
        {

            @Override
            public Map<String, String> apply(ConnectAddonBean addonBean)
            {
                return buildKeyMap(addonBean);
            }
        });

        return keyMap.getOrDefault(location, location);
    }

    private ImmutableMap<String, String> buildKeyMap(ConnectAddonBean addon)
    {
        ImmutableMap.Builder<String, String> keyMapBuilder = ImmutableMap.<String, String>builder();

        Consumer<Exception> moduleValidationExceptionHandler = new LoggingModuleValidationExceptionHandler();
        Optional<List<ModuleBean>> optionalWebItems = addon.getModules().getValidModuleListOfType("webItems", moduleValidationExceptionHandler);
        optionalWebItems.ifPresent(new Consumer<List<ModuleBean>>()
        {

            @Override
            public void accept(List<ModuleBean> webItems)
            {
                keyMapBuilder.putAll(WebFragmentLocationQualifierImpl.this.createKeyToQualifiedKeyMap(addon, webItems));
            }
        });

        Optional<List<ModuleBean>> optionalWebSections = addon.getModules().getValidModuleListOfType("webSections", moduleValidationExceptionHandler);
        optionalWebSections.ifPresent(new Consumer<List<ModuleBean>>()
        {

            @Override
            public void accept(List<ModuleBean> webSections)
            {
                keyMapBuilder.putAll(WebFragmentLocationQualifierImpl.this.createKeyToQualifiedKeyMap(addon, webSections));
            }
        });

        return keyMapBuilder.build();
    }

    private Map<String, String> createKeyToQualifiedKeyMap(ConnectAddonBean addon, List<ModuleBean> modules)
    {
        List<RequiredKeyBean> requiredKeyBeans = modules.stream()
                .filter(new Predicate<ModuleBean>()
                {

                    @Override
                    public boolean test(ModuleBean module)
                    {
                        return module instanceof RequiredKeyBean;
                    }
                })
                .map(new Function<ModuleBean, RequiredKeyBean>()
                {

                    @Override
                    public RequiredKeyBean apply(ModuleBean bean)
                    {
                        return RequiredKeyBean.class.cast(bean);
                    }
                })
                .collect(Collectors.toList());
        ImmutableMap<String, RequiredKeyBean> rawKeyMap = Maps.uniqueIndex(requiredKeyBeans, new com.google.common.base.Function<RequiredKeyBean, String>()
        {

            @Override
            public String apply(@Nullable RequiredKeyBean requiredKeyBean)
            {
                return requiredKeyBean.getRawKey();
            }
        });
        return Maps.transformValues(rawKeyMap, new com.google.common.base.Function<RequiredKeyBean, String>()
        {

            @Override
            public String apply(@Nullable RequiredKeyBean requiredKeyBean)
            {
                return requiredKeyBean.getKey(addon);
            }
        });
    }
}
