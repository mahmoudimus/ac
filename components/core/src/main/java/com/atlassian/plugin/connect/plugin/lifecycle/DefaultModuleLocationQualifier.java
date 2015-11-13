package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.atlassian.plugin.connect.api.web.item.ModuleLocationQualifier;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultModuleLocationQualifier implements ModuleLocationQualifier
{
    private static final char LOCATION_SEGMENT_SEPARATOR = '/';
    private static final Joiner SEGMENT_JOINER = Joiner.on(LOCATION_SEGMENT_SEPARATOR);
    private static final Splitter SEGMENT_SPLITTER = Splitter.on(LOCATION_SEGMENT_SEPARATOR);

    // a map of unqualified key -> qualified key for all modules that can be referenced from the locations of other
    // modules.
    private final Supplier<Map<String, String>> keyMapSupplier;
    private final ConnectAddonBean addonBean;

    public DefaultModuleLocationQualifier(final ConnectAddonBean addonBean)
    {
        this.addonBean = addonBean;
        this.keyMapSupplier = Suppliers.memoize(() -> buildKeyMap(addonBean));
    }

    private ImmutableMap<String, String> buildKeyMap(ConnectAddonBean addonBean)
    {
        ImmutableMap.Builder<String, String> keyMapBuilder = ImmutableMap.<String, String>builder();

        Optional<List<ModuleBean>> optionalWebItems = addonBean.getModules().getValidModuleListOfType("webItems");
        optionalWebItems.ifPresent((webItems) -> keyMapBuilder.putAll(createKeyToQualifiedKeyMap(webItems)));

        Optional<List<ModuleBean>> optionalWebSections = addonBean.getModules().getValidModuleListOfType("webSections");
        optionalWebSections.ifPresent((webSections) -> keyMapBuilder.putAll(createKeyToQualifiedKeyMap(webSections)));

        return keyMapBuilder.build();
    }

    private Map<String, String> createKeyToQualifiedKeyMap(@Nullable List<ModuleBean> modules)
    {
        if (modules == null)
        {
            return new HashMap<>();
        }

        List<RequiredKeyBean> requiredKeyBeans = modules.stream()
                .filter((module) -> module instanceof RequiredKeyBean)
                .map(RequiredKeyBean.class::cast)
                .collect(Collectors.toList());
        ImmutableMap<String, RequiredKeyBean> rawKeyMap = Maps.uniqueIndex(requiredKeyBeans, RequiredKeyBean::getRawKey);
        return Maps.transformValues(rawKeyMap, (requiredKeyBean) -> requiredKeyBean.getKey(addonBean));
    }

    @Override
    public String processLocation(String location)
    {
        final Iterable<String> processedSegments = Iterables.transform(SEGMENT_SPLITTER.split(location),
                new Function<String, String>()
                {
                    @Override
                    public String apply(@Nullable String segment)
                    {
                        return processSegment(segment);
                    }
                });

        return SEGMENT_JOINER.join(processedSegments);
    }

    private String processSegment(String location)
    {
        final Map<String, String> map = keyMapSupplier.get();

        final String qualifiedLocation = map.get(location);
        return qualifiedLocation != null ? qualifiedLocation : location;
    }
}
