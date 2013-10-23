package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.AbstractConnectTabPanelModuleDescriptorFactory;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.TabPanelCapabilityBeanMatchers.hasAddonKeyValue;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.TabPanelCapabilityBeanMatchers.hasAddonNameI18KeyValue;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.TabPanelCapabilityBeanMatchers.hasAddonNameValue;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.TabPanelCapabilityBeanMatchers.hasUrlValue;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.TabPanelCapabilityBeanMatchers.hasWeightValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractConnectTabPanelModuleProviderTest<T extends AbstractConnectTabPanelModuleDescriptorFactory>
{
    protected static final String ADDON_KEY = "myKey";
    protected static final String ADDON_NAME = "myName";
    protected static final String ADDON_URL = "/myUrl";
    protected static final String ADDON_I18_NAME_KEY = "myi18key";
    protected static final int WEIGHT = 99;

    @Mock
    protected Plugin plugin;
    @Mock
    protected BundleContext bundleContext;

    protected ModuleDescriptor descriptor;

    private final AbstractConnectTabPanelCapabilityBean bean;

    private final Class<? extends ModuleDescriptor<?>> descriptorClass;

    private final Class<T> factoryClass;

    protected T moduleDescriptorFactory;


    protected AbstractConnectTabPanelModuleProviderTest(Class<? extends ModuleDescriptor<?>> descriptorClass,
                                                        Class<T> factoryClass)
    {
        this.descriptorClass = descriptorClass;
        this.factoryClass = factoryClass;
        bean = createCapabilityBean();
    }

    @Before
    public void init()
    {
        moduleDescriptorFactory = mock(factoryClass);
        descriptor = mock(descriptorClass);
        when(moduleDescriptorFactory.createModuleDescriptor(eq(plugin), eq(bundleContext), any(AbstractConnectTabPanelCapabilityBean.class)))
                .thenReturn(descriptor);
    }

    @Test
    public void producesTheExpectedDescriptor()
    {
        assertThat(providedModules(), Matchers.<ModuleDescriptor>contains(descriptor));
    }

    @Test
    public void callsDescriptorFactoryWithExpectedArgs()
    {
        verify(moduleDescriptorFactory(), times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), any(AbstractConnectTabPanelCapabilityBean.class));
    }

    @Test
    public void capabilityBeanHasCorrectKey()
    {
        verify(moduleDescriptorFactory(), times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonKeyValue(ADDON_KEY)));
    }

    @Test
    public void capabilityBeanHasCorrectName()
    {
        verify(moduleDescriptorFactory(), times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonNameValue(ADDON_NAME)));
    }

    @Test
    public void capabilityBeanHasCorrectI18NameKey()
    {
        verify(moduleDescriptorFactory(), times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonNameI18KeyValue(ADDON_I18_NAME_KEY)));
    }

    @Test
    public void capabilityBeanHasCorrectUrl()
    {
        verify(moduleDescriptorFactory(), times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasUrlValue(ADDON_URL)));
    }

    @Test
    public void capabilityBeanHasCorrectWeight()
    {
        verify(moduleDescriptorFactory(), times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasWeightValue(WEIGHT)));
    }

    protected abstract AbstractConnectTabPanelCapabilityBean createCapabilityBean();

    protected abstract AbstractConnectTabPanelModuleProvider createProvider();

    private List<ModuleDescriptor> providedModules()
    {
        return createProvider().provideModules(plugin, bundleContext, ImmutableList.of(bean));
    }

    private T moduleDescriptorFactory()
    {
        providedModules();
        return moduleDescriptorFactory;
    }
}
