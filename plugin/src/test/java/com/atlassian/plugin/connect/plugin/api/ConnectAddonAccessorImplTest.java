package com.atlassian.plugin.connect.plugin.api;

import com.atlassian.plugin.connect.api.installer.AddonSettings;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectAddonAccessorImplTest
{

    @InjectMocks
    private ConnectAddonAccessorImpl addonAccessor;

    @Mock
    private ConnectAddonRegistry addonRegistry;

    @Mock
    private ConnectAddonBeanFactory addonBeanFactory;

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

        when(addonRegistry.getAllAddonSettings()).thenReturn(settings);
        when(addonBeanFactory.fromJson(jsonDescriptors[0])).thenReturn(beans.get(0));
        when(addonBeanFactory.fromJson(jsonDescriptors[1])).thenReturn(beans.get(1));

        assertThat(addonAccessor.getAllAddons(), contains(beans.toArray(new ConnectAddonBean[beans.size()])));
    }

    @Test
    public void shouldReturnAddonBeanIfRegistryHasDescriptor()
    {
        String addonKey = "foo";
        String jsonDescriptor = "foo-json";
        ConnectAddonBean addonBean = mock(ConnectAddonBean.class);

        when(addonRegistry.getDescriptor(addonKey)).thenReturn(jsonDescriptor);
        when(addonBeanFactory.fromJson(jsonDescriptor)).thenReturn(addonBean);

        assertThat(addonAccessor.getAddon(addonKey), equalTo(Optional.of(addonBean)));
    }

    @Test
    public void shouldReturnNoAddonBeanIfRegistryLacksDescriptor()
    {
        String addonKey = "foo";

        when(addonRegistry.getDescriptor(addonKey)).thenReturn(null);

        assertThat(addonAccessor.getAddon(addonKey), equalTo(Optional.empty()));
    }

    private AddonSettings createAddonSettingsForDescriptor(String descriptor)
    {
        AddonSettings settings = new AddonSettings();
        settings.setDescriptor(descriptor);
        return settings;
    }
}
