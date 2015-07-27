package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.installer.AddonSettings;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.DefaultConnectAddonRegistry.ADDON_KEY_PREFIX;
import static com.atlassian.plugin.connect.plugin.DefaultConnectAddonRegistry.ADDON_LIST_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(pluginSettings);

        this.registry = new DefaultConnectAddonRegistry(pluginSettingsFactory, connectAddonBeanFactory);
    }

    @Test
    public void shouldReturnNoAddonKeys()
    {
        List<String> keys = Collections.emptyList();
        when(pluginSettings.get(any(String.class))).thenReturn(keys);
        assertThat(this.registry.getAllAddonKeys(), emptyIterable());
        assertThat(this.registry.hasAddons(), is(false));
    }

    @Test
    public void shouldReturnAddonKeys()
    {
        String[] keys = new String[]{"foo", "bar"};
        List<String> keyList = Arrays.asList(keys);
        when(pluginSettings.get(any(String.class))).thenReturn(keyList);
        assertThat(this.registry.getAllAddonKeys(), containsInAnyOrder(keys));
        assertThat(this.registry.hasAddons(), is(true));
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

        when(pluginSettings.get(any(String.class))).thenReturn(
                keyList,
                toJson(settings.get(0)),
                toJson(settings.get(1))
        );
        when(connectAddonBeanFactory.fromJsonSkipValidation(jsonDescriptors[0])).thenReturn(beans.get(0));
        when(connectAddonBeanFactory.fromJsonSkipValidation(jsonDescriptors[1])).thenReturn(beans.get(1));

        assertThat(this.registry.getAllAddonBeans(), contains(beans.toArray(new ConnectAddonBean[beans.size()])));
    }

    @Test
    public void shouldReturnAddonBean()
    {
        String key = "foo";
        String jsonDescriptor = "foo-json";
        ConnectAddonBean bean = mock(ConnectAddonBean.class);
        AddonSettings settings = this.createAddonSettingsForDescriptor(jsonDescriptor);

        when(pluginSettings.get(any(String.class))).thenReturn(toJson(settings));
        when(connectAddonBeanFactory.fromJsonSkipValidation(jsonDescriptor)).thenReturn(bean);

        assertThat(this.registry.getAddonBean(key), equalTo(Option.some(bean)));
    }

    @Test
    public void shouldStoreRestartStateWhenChanged()
    {
        String addonKey = "foo";
        AddonSettings addonSettings = new AddonSettings();

        when(pluginSettings.get(addonKey)).thenReturn(toJson(addonSettings));
        when(pluginSettings.get(ADDON_LIST_KEY)).thenReturn(ImmutableList.of(addonKey));

        registry.storeRestartState(addonKey, PluginState.DISABLED);
        addonSettings.setRestartState(PluginState.DISABLED);

        verify(pluginSettings).put(ADDON_KEY_PREFIX + addonKey, toJson(addonSettings));
        verify(pluginSettings, never()).put(eq(ADDON_LIST_KEY), any());
    }

    @Test
    public void shouldNotStoreRestartStateWhenUnchanged()
    {
        String addonKey = "foo";
        AddonSettings addonSettings = new AddonSettings();

        when(pluginSettings.get(addonKey)).thenReturn(toJson(addonSettings));
        when(pluginSettings.get(ADDON_LIST_KEY)).thenReturn(ImmutableList.of(addonKey));

        registry.storeRestartState(addonKey, PluginState.ENABLED);

        verify(pluginSettings, never()).put(eq(ADDON_KEY_PREFIX) + addonKey, any());
        verify(pluginSettings, never()).put(eq(ADDON_LIST_KEY), any());
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
