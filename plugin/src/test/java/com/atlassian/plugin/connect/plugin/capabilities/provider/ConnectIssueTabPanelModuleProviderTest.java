package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectIssueTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptorFactory;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
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
public class ConnectIssueTabPanelModuleProviderTest
{
    private static final String ADDON_KEY = "myKey";
    private static final String ADDON_NAME = "myName";
    private static final String ADDON_URL = "/myUrl";
    private static final String ADDON_I18_NAME_KEY = "myi18key";
    private static final int WEIGHT = 99;

    @Mock
    private ConnectIssueTabPanelModuleDescriptorFactory moduleDescriptorFactory;

    @Mock
    private Plugin plugin;

    @Mock
    private BundleContext bundleContext;

    @Mock private ConnectIssueTabPanelModuleDescriptor descriptor;

    private final ConnectIssueTabPanelCapabilityBean bean = new ConnectIssueTabPanelCapabilityBeanBuilder()
            .withKey(ADDON_KEY)
            .withName(new I18nProperty(ADDON_NAME, ADDON_I18_NAME_KEY))
            .withWeight(WEIGHT)
            .withUrl(ADDON_URL)
            .build();

    @Test
    public void producesTheExpectedDescriptor()
    {
        assertThat(providedModules(), Matchers.<ModuleDescriptor>contains(descriptor));
    }

    @Test
    public void callsDescriptorFactoryWithExpectedArgs()
    {
        providedModules();
        verify(moduleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), any(ConnectIssueTabPanelCapabilityBean.class));
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

    private ConnectIssueTabPanelCapabilityBean capturedCapabilityBean()
    {
        ConnectIssueTabPanelModuleProvider provider = createProvider();

        List<ModuleDescriptor> moduleDescriptors = provider.provideModules(plugin, bundleContext, ImmutableList.of(bean));

        assertThat(moduleDescriptors, hasItem(descriptor));

        ArgumentCaptor<ConnectIssueTabPanelCapabilityBean> argumentCaptor = ArgumentCaptor.forClass(ConnectIssueTabPanelCapabilityBean.class);
        verify(moduleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    private ConnectIssueTabPanelModuleProvider createProvider() {
        ConnectIssueTabPanelModuleProvider provider = new ConnectIssueTabPanelModuleProvider(moduleDescriptorFactory);
        when(moduleDescriptorFactory.createModuleDescriptor(eq(plugin), eq(bundleContext), any(ConnectIssueTabPanelCapabilityBean.class)))
                .thenReturn(descriptor);

        return provider;
    }
    private List<ModuleDescriptor> providedModules()
    {
        return createProvider().provideModules(plugin, bundleContext, ImmutableList.of(bean));
    }

}
