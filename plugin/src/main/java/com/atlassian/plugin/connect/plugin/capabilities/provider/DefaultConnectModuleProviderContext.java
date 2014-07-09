package com.atlassian.plugin.connect.plugin.capabilities.provider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class DefaultConnectModuleProviderContext implements ConnectModuleProviderContext
{
    private final ConnectAddonBean addonBean;
    private final DefaultConnectMenuHelper menuHelper;

    public DefaultConnectModuleProviderContext(ConnectAddonBean addonBean)
    {
        this.addonBean = addonBean;
        this.menuHelper = new DefaultConnectMenuHelper();
    }

    @Override
    public ConnectAddonBean getConnectAddonBean()
    {
        return addonBean;
    }

    @Override
    public ConnectMenuHelper getMenuHelper()
    {
        return menuHelper;
    }

    private class DefaultConnectMenuHelper implements ConnectMenuHelper
    {

        private Supplier<Map<String, String>> webItemKeySupplier = Suppliers.memoize(new Supplier<Map<String, String>>()
        {
            @Override
            public Map<String, String> get()
            {
                return createKeyToQualifiedKeyMap(getConnectAddonBean().getModules().getWebItems());
            }
        });

        private <T extends RequiredKeyBean> Map<String, String> createKeyToQualifiedKeyMap(List<T> beans)
        {
            final ImmutableMap<String, T> map =
                    Maps.uniqueIndex(beans, new Function<T, String>()
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
                public String apply(@Nullable T input)
                {
                    return addonBean.getKey();
                }
            });
        }


        @Override
        public String processLocation(String location)
        {
            // TODO: include web section keys in same map
            final Map<String, String> map = webItemKeySupplier.get();

            // TODO: logic for splitting on '/'
            final String qualifiedLocation = map.get(location);
            return qualifiedLocation != null ? qualifiedLocation : location;
        }
    }
}
