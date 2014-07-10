package com.atlassian.plugin.connect.plugin.capabilities.provider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class DefaultConnectModuleProviderContext implements ConnectModuleProviderContext
{
    private final ConnectAddonBean addonBean;
    private final DefaultModuleLocationQualifier locationQualifier;

    public DefaultConnectModuleProviderContext(ConnectAddonBean addonBean)
    {
        this.addonBean = addonBean;
        this.locationQualifier = new DefaultModuleLocationQualifier();
    }

    @Override
    public ConnectAddonBean getConnectAddonBean()
    {
        return addonBean;
    }

    @Override
    public ModuleLocationQualifier getLocationQualifier()
    {
        return locationQualifier;
    }

    private class DefaultModuleLocationQualifier implements ModuleLocationQualifier
    {
        private static final char LOCATION_SEGMENT_SEPARATOR = '/';
        
        // a map of unqualified key -> qualified key for all modules that can be referenced from the locations of other
        // modules.
        private Supplier<Map<String, String>> keyMapSupplier = Suppliers.memoize(new Supplier<Map<String, String>>()
        {
            @Override
            public Map<String, String> get()
            {
                return ImmutableMap.<String, String>builder()
                        .putAll(createKeyToQualifiedKeyMap(getConnectAddonBean().getModules().getWebItems()))
                        .putAll(createKeyToQualifiedKeyMap(getConnectAddonBean().getModules().getWebSections()))
                        .build();
            }
        });


        private <T extends RequiredKeyBean> Map<String, String> createKeyToQualifiedKeyMap(List<T> beans)
        {
            final ImmutableMap<String, T> map = Maps.uniqueIndex(beans, new Function<T, String>()
            {
                @Override
                public String apply(@Nullable T bean)
                {
                    return bean.getRawKey();
                }
            });

            return Maps.transformValues(map, new Function<T, String>()
            {
                @Override
                public String apply(@Nullable T bean)
                {
                    return bean.getKey(addonBean);
                }
            });
        }


        @Override
        public String processLocation(String location)
        {
            final Iterable<String> processedSegments = Iterables.transform(Splitter.on(LOCATION_SEGMENT_SEPARATOR).split(location),
                    new Function<String, String>()
            {
                @Override
                public String apply(@Nullable String segment)
                {
                    return processSegment(segment);
                }
            });

            return Joiner.on(LOCATION_SEGMENT_SEPARATOR).join(processedSegments);
        }

        private String processSegment(String location)
        {
            final Map<String, String> map = keyMapSupplier.get();

            final String qualifiedLocation = map.get(location);
            return qualifiedLocation != null ? qualifiedLocation : location;
        }
    }
}
