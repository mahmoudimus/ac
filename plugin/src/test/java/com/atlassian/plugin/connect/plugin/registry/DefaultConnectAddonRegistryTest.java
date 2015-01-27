package com.atlassian.plugin.connect.plugin.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.installer.AddonSettings;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.any;

import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConnectAddonRegistryTest
{

    private DefaultConnectAddonRegistry registry;

    @Mock
    private PluginSettingsFactory pluginSettingsFactory;

    @Mock
    private ConnectAddonBeanFactory connectAddonBeanFactory;

    @Mock
    private PluginSettings pluginSettings;

    @Before
    public void setUp()
    {
        this.registry = new DefaultConnectAddonRegistry(this.pluginSettingsFactory, this.connectAddonBeanFactory);

        when(this.pluginSettingsFactory.createGlobalSettings()).thenReturn(this.pluginSettings);
    }

    @Test
    public void shouldReturnNoAddonKeys()
    {
        List<String> keys = Collections.emptyList();
        when(this.pluginSettings.get(any(String.class))).thenReturn(keys);
        assertThat(this.registry.getAllAddonKeys(), emptyIterable());
    }

    @Test
    public void shouldReturnAddonKeys()
    {
        String[] keys = new String[]{"foo", "bar"};
        List<String> keyList = Arrays.asList(keys);
        when(this.pluginSettings.get(any(String.class))).thenReturn(keyList);
        assertThat(this.registry.getAllAddonKeys(), containsInAnyOrder(keys));
    }

    @Test
    public void shouldReturnAddonBeans()
    {
        String[] keys = new String[]{"foo", "bar"};
        String[] jsonDescriptors = new String[]{"foo-json", "bar-json"};
        List<String> keyList = Arrays.asList(keys);
        List<ConnectAddonBean> beans = Lists.newArrayList(
                mock(ConnectAddonBean.class),
                mock(ConnectAddonBean.class)
        );
        List<AddonSettings> settings = Lists.newArrayList(
                this.createAddonSettingsForDescriptor(jsonDescriptors[0]),
                this.createAddonSettingsForDescriptor(jsonDescriptors[1])
        );

        when(this.pluginSettings.get(any(String.class))).thenReturn(
                keyList,
                toJson(settings.get(0)),
                toJson(settings.get(1))
        );
        when(this.connectAddonBeanFactory.fromJsonSkipValidation(jsonDescriptors[0])).thenReturn(beans.get(0));
        when(this.connectAddonBeanFactory.fromJsonSkipValidation(jsonDescriptors[1])).thenReturn(beans.get(1));

        assertThat(this.registry.getAllAddonBeans(), contains(beans.toArray(new ConnectAddonBean[beans.size()])));
    }

    @Test
    public void shouldReturnAddonBean()
    {
        String key = "foo";
        String jsonDescriptor = "foo-json";
        ConnectAddonBean bean = mock(ConnectAddonBean.class);
        AddonSettings settings = this.createAddonSettingsForDescriptor(jsonDescriptor);

        when(this.pluginSettings.get(any(String.class))).thenReturn(toJson(settings));
        when(this.connectAddonBeanFactory.fromJsonSkipValidation(jsonDescriptor)).thenReturn(bean);

        assertThat(this.registry.getAddonBean(key), equalTo(Option.some(bean)));
    }

    private AddonSettings createAddonSettingsForDescriptor(String descriptor)
    {
        AddonSettings settings = new AddonSettings();
        settings.setDescriptor(descriptor);
        return settings;
    }

    private String toJson(Object object)
    {
        return new Gson().toJson(object);
    }
}
