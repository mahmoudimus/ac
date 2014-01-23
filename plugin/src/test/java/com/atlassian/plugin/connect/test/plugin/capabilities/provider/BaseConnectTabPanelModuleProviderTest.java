package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseConnectTabPanelModuleProviderTest
{
    protected static final String ADDON_KEY = "myKey";
    protected static final String ADDON_NAME = "myName";
    protected static final String ADDON_URL = "/myUrl";
    protected static final String ADDON_I18_NAME_KEY = "myi18key";

    @Mock
    protected Plugin plugin;
    @Mock
    protected BundleContext bundleContext;
    
    protected ConnectTabPanelModuleDescriptorFactory moduleDescriptorFactory;
    protected ConnectTabPanelModuleProvider moduleProvider;

    private final Class<? extends ModuleDescriptor<?>> expectedDescriptorClass;
    private ModuleDescriptor<?> expectedDescriptor;
    private final String jsonFieldName;
    

    protected BaseConnectTabPanelModuleProviderTest(Class<? extends ModuleDescriptor<?>> expectedDescriptorClass, String jsonFieldName)
    {
        this.expectedDescriptorClass = expectedDescriptorClass;
        this.jsonFieldName = jsonFieldName;
    }

    @Before
    public void init()
    {
        ConnectAutowireUtil connectAutowireUtil = mock(ConnectAutowireUtil.class);
        ConditionModuleFragmentFactory conditionModuleFragmentFactory = mock(ConditionModuleFragmentFactory.class);
        
        when(connectAutowireUtil.createBean(any(Class.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return mock((Class)invocation.getArguments()[0]);
            }
        });
        
        moduleDescriptorFactory = new ConnectTabPanelModuleDescriptorFactory(conditionModuleFragmentFactory,connectAutowireUtil);
        moduleProvider = new ConnectTabPanelModuleProvider(moduleDescriptorFactory);
        expectedDescriptor = mock(expectedDescriptorClass);
    }

    /*
     * NOTE: this was the only test from the old code that was actually testing the provider.
     * All other tests were actually testing the descriptorFactory which is covered by a different test(s)
     */
    @Test
    public void producesTheExpectedDescriptor()
    {
        assertThat(providedModules(), Matchers.contains(Matchers.hasProperty("class",Matchers.equalTo(expectedDescriptor.getClass()))));
    }
    
    private List<ModuleDescriptor> providedModules()
    {
        return moduleProvider.provideModules(plugin, bundleContext, jsonFieldName, ImmutableList.of(createBean()));
    }
    
    private ConnectTabPanelModuleBean createBean()
    {
        return newTabPanelBean()
                .withName(new I18nProperty(ADDON_NAME, ADDON_I18_NAME_KEY))
                .withKey(ADDON_KEY)
                .withUrl(ADDON_URL)
                .withWeight(99)
                .build();
    }
}
