package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectVersionTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionTabPanelModuleDescriptorFactory;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectVersionTabPanelModuleProviderTest
{
    private static final String ADDON_KEY = "myKey";
    private static final String ADDON_NAME = "myName";
    private static final String ADDON_URL = "/myUrl";
    private static final String ADDON_I18_NAME_KEY = "myi18key";
    private static final int WEIGHT = 99;

    @Mock
    private ConnectVersionTabPanelModuleDescriptorFactory moduleDescriptorFactory;
    @Mock
    private Plugin plugin;
    @Mock
    private BundleContext bundleContext;
    @Mock private ConnectVersionTabPanelModuleDescriptor descriptor;

    private final ConnectVersionTabPanelCapabilityBean bean = new ConnectVersionTabPanelCapabilityBeanBuilder()
            .withKey(ADDON_KEY)
            .withName(new I18nProperty(ADDON_NAME, ADDON_I18_NAME_KEY))
            .withWeight(WEIGHT)
            .withUrl(ADDON_URL)
            .build();

    @Test
    public void createsASingleDescriptor()
    {
        ConnectVersionTabPanelModuleProvider provider = createProvider();

        List<ModuleDescriptor> moduleDescriptors = provider.provideModules(plugin, bundleContext, ImmutableList.of(bean));

        assertThat(moduleDescriptors, hasItem(descriptor));
    }

    @Test
    public void callsDescriptorFactoryWithExpectedArgs()
    {
        ConnectVersionTabPanelModuleProvider provider = createProvider();

        List<ModuleDescriptor> moduleDescriptors = provider.provideModules(plugin, bundleContext, ImmutableList.of(bean));

        assertThat(moduleDescriptors, hasItem(descriptor));

        verify(moduleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), any(ConnectVersionTabPanelCapabilityBean.class));
    }

    @Test
    public void capabilityBeanHasCorrectKey()
    {
        assertThat(capturedCapabilityBean().getKey(), is(equalTo(ADDON_KEY)));
    }

    @Test
    public void capabilityBeanHasCorrectName()
    {
        assertThat(capturedCapabilityBean().getName().getValue(), is(equalTo(ADDON_NAME)));
    }

    @Test
    public void capabilityBeanHasCorrectI18NameKey()
    {
        assertThat(capturedCapabilityBean().getName().getI18n(), is(equalTo(ADDON_I18_NAME_KEY)));
    }

    @Test
    public void capabilityBeanHasCorrectUrl()
    {
        assertThat(capturedCapabilityBean().getUrl(), is(equalTo(ADDON_URL)));
    }

    @Test
    public void capabilityBeanHasCorrectWeight()
    {
        assertThat(capturedCapabilityBean().getWeight(), is(equalTo(WEIGHT)));
    }

    private ConnectVersionTabPanelCapabilityBean capturedCapabilityBean()
    {
        ConnectVersionTabPanelModuleProvider provider = createProvider();

        List<ModuleDescriptor> moduleDescriptors = provider.provideModules(plugin, bundleContext, ImmutableList.of(bean));

        assertThat(moduleDescriptors, hasItem(descriptor));

        ArgumentCaptor<ConnectVersionTabPanelCapabilityBean> argumentCaptor = ArgumentCaptor.forClass(ConnectVersionTabPanelCapabilityBean.class);
        verify(moduleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    private ConnectVersionTabPanelModuleProvider createProvider() {
        ConnectVersionTabPanelModuleProvider provider = new ConnectVersionTabPanelModuleProvider(moduleDescriptorFactory);
        when(moduleDescriptorFactory.createModuleDescriptor(eq(plugin), eq(bundleContext), any(ConnectVersionTabPanelCapabilityBean.class)))
                .thenReturn(descriptor);

        return provider;
    }
}
