package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelCapabilityBean.newProjectAdminTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.WebItemCapabilityBeanMatchers.hasAddonKeyValue;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.WebItemCapabilityBeanMatchers.hasAddonNameI18KeyValue;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.WebItemCapabilityBeanMatchers.hasAddonNameValue;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.WebItemCapabilityBeanMatchers.hasUrlValue;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.WebItemCapabilityBeanMatchers.hasWeightValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectProjectAdminTabPanelModuleProviderTest
{
    private static final String ADDON_KEY = "myKey";
    private static final String ADDON_NAME = "myName";
    private static final String ADDON_URL = "/myUrl";
    private static final String EXPECTED_IFRAME_URL = "xx/myUrl";
    private static final String ADDON_I18_NAME_KEY = "myi18key";
    private static final int WEIGHT = 99;
    private static final String LOCATION = "a-location";
    private static final String EXPECTED_LOCATION = "atl.jira.proj.config/a-location";
    private static final String EXPECTED_DECORATOR = "what should the decorator be";
    private static final String EXPECTED_TEMPLATE_SUFFIX = "what should the template suffix be";
    private static final Condition EXPECTED_CONDITION = new Condition()
    {
        @Override
        public void init(Map<String, String> params) throws PluginParseException
        {
        }

        @Override
        public boolean shouldDisplay(Map<String, Object> context)
        {
            return false;
        }

        @Override
        public boolean equals(Object obj)
        {
            return false; // setting this up to always fail till I figure out what condition to expect
        }
    };
    private static final Map<String, String> EXPECTED_META_TAGS = ImmutableMap.of("what", "should go in the meta tags");

    @Mock
    private Plugin plugin;

    @Mock
    private BundleContext bundleContext;

    @Mock
    private WebItemModuleDescriptor webItemDescriptor;

    @Mock
    private ServletModuleDescriptor servletModuleDescriptor;

    @Mock
    private RelativeAddOnUrlConverter relativeAddOnUrlConverter;

    @Mock
    private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;

    @Mock
    private IFramePageServletDescriptorFactory servletDescriptorFactory;

    private ConnectProjectAdminTabPanelModuleProvider projectAdminTabPanelModuleProvider;
    private ConnectProjectAdminTabPanelCapabilityBean bean;


    @Before
    public void init()
    {
        when(plugin.getKey()).thenReturn(ADDON_KEY);
        when(plugin.getName()).thenReturn(ADDON_NAME);

        when(webItemModuleDescriptorFactory.createModuleDescriptor(eq(plugin), eq(bundleContext), any(WebItemCapabilityBean.class)))
                .thenReturn(webItemDescriptor);

        when(servletDescriptorFactory.createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class), anyString(),
                anyString(), anyString(), anyString(), any(Condition.class), anyMap())).thenReturn(servletModuleDescriptor);

        when(relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(ADDON_KEY, ADDON_URL)).thenReturn(EXPECTED_IFRAME_URL);

        projectAdminTabPanelModuleProvider = new ConnectProjectAdminTabPanelModuleProvider(webItemModuleDescriptorFactory,
                servletDescriptorFactory, relativeAddOnUrlConverter);
        bean = createCapabilityBean();
        providedModules();
    }

    @Test
    public void producesTheExpectedDescriptor()
    {
        assertThat(providedModules(), Matchers.<ModuleDescriptor>contains(webItemDescriptor, servletModuleDescriptor));
    }

    @Test
    public void capabilityBeanHasCorrectKey()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonKeyValue(ADDON_KEY)));
    }

    @Test
    public void capabilityBeanHasCorrectName()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonNameValue(ADDON_NAME)));
    }

    @Test
    public void capabilityBeanHasCorrectI18NameKey()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonNameI18KeyValue(ADDON_I18_NAME_KEY)));
    }

    @Test
    public void capabilityBeanHasCorrectUrl()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasUrlValue(EXPECTED_IFRAME_URL)));
    }

    @Test
    public void capabilityBeanHasCorrectWeight()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasWeightValue(WEIGHT)));
    }

    //        when(servletDescriptorFactory.createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class), eq(EXPECTED_IFRAME_URL),
//                eq(ADDON_URL), "atl.general", "", new AlwaysDisplayCondition(), ImmutableMap.<String, String>of())).thenReturn(servletModuleDescriptor);
//        when(servletDescriptorFactory.createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class), anyString(),
//    anyString(), anyString(), anyString(), any(Condition.class), anyMap())).thenReturn(servletModuleDescriptor);

    @Test
    public void callsServletDescriptorFactoryWithCorrectLocalUrl()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class),
                eq(EXPECTED_IFRAME_URL), anyString(), anyString(), anyString(), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectAddonUrl()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class),
                anyString(), eq(ADDON_URL), anyString(), anyString(), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectLocation()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class),
                anyString(), anyString(), eq(EXPECTED_LOCATION), anyString(), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectDecorator()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class),
                anyString(), anyString(), anyString(), eq(EXPECTED_DECORATOR), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectTemplateSuffix()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class),
                anyString(), anyString(), anyString(), eq(EXPECTED_TEMPLATE_SUFFIX), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectCondition()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class),
                anyString(), anyString(), anyString(), anyString(), eq(EXPECTED_CONDITION), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectTemplateMetaTags()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameServletDescriptor(eq(plugin), any(WebItemCapabilityBean.class),
                anyString(), anyString(), anyString(), anyString(), any(Condition.class), eq(EXPECTED_META_TAGS));
    }

    private ConnectProjectAdminTabPanelCapabilityBean createCapabilityBean()
    {
        return newProjectAdminTabPanelBean()
                .withKey(ADDON_KEY)
                .withName(new I18nProperty(ADDON_NAME, ADDON_I18_NAME_KEY))
                .withUrl(ADDON_URL)
                .withWeight(WEIGHT)
                .withLocation(LOCATION)
                .build();
    }

    private List<ModuleDescriptor> providedModules()
    {
        return projectAdminTabPanelModuleProvider.provideModules(plugin, bundleContext, ImmutableList.of(bean));
    }

}