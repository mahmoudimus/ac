package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrl;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.module.jira.conditions.IsProjectAdminCondition;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.uri.Uri;
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

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.WebItemModuleBeanMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectProjectAdminTabPanelModuleProviderTest
{
    private static final String JSON_FIELD_NAME = "projectAdminTabPanels";
    
    private static final String ADDON_KEY = "myKey";
    private static final String ADDON_NAME = "myName";
    private static final String ADDON_URL = "/myUrl";
    private static final String EXPECTED_IFRAME_URL = "/plugins/servlet/xx/myUrl?projectKey={project.key}";
    private static final String EXPECTED_IFRAME_DESCRIPTOR_URL = "/xx/myUrl";
    private static final RelativeAddOnUrl EXPECTED_IFRAME_URL_HOLDER = new RelativeAddOnUrl(Uri.parse(EXPECTED_IFRAME_DESCRIPTOR_URL));
    private static final String ADDON_I18_NAME_KEY = "myi18key";
    private static final int WEIGHT = 99;
    private static final String LOCATION = "a-location";
    private static final String EXPECTED_LOCATION = "atl.jira.proj.config/a-location";
    private static final String EXPECTED_DECORATOR = "";
    private static final String EXPECTED_TEMPLATE_SUFFIX = "-project-admin";
    private static final Map<String, String> EXPECTED_META_TAGS = ImmutableMap.of("adminActiveTab", ADDON_KEY);

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
    private ConnectProjectAdminTabPanelModuleBean bean;


    @Before
    public void init()
    {
        when(plugin.getKey()).thenReturn(ADDON_KEY);
        when(plugin.getName()).thenReturn(ADDON_NAME);

        when(webItemModuleDescriptorFactory.createModuleDescriptor(eq(plugin), eq(bundleContext), any(WebItemModuleBean.class)))
                .thenReturn(webItemDescriptor);

        when(servletDescriptorFactory.createIFrameProjectConfigTabServletDescriptor(eq(plugin), any(WebItemModuleBean.class), anyString(),
                anyString(), anyString(), anyString(), any(Condition.class), anyMap())).thenReturn(servletModuleDescriptor);

        when(relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(ADDON_KEY, ADDON_URL)).thenReturn(EXPECTED_IFRAME_URL_HOLDER);

        projectAdminTabPanelModuleProvider = new ConnectProjectAdminTabPanelModuleProvider(webItemModuleDescriptorFactory,
                servletDescriptorFactory, relativeAddOnUrlConverter, mock(JiraAuthenticationContext.class));
        bean = createModuleBean();
        providedModules();
    }

    @Test
    public void producesTheExpectedDescriptor()
    {
        assertThat(providedModules(), Matchers.<ModuleDescriptor>contains(webItemDescriptor, servletModuleDescriptor));
    }

    @Test
    public void moduleBeanHasCorrectKey()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonKeyValue(ADDON_KEY)));
    }

    @Test
    public void moduleBeanHasCorrectName()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonNameValue(ADDON_NAME)));
    }

    @Test
    public void moduleBeanHasCorrectI18NameKey()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasAddonNameI18KeyValue(ADDON_I18_NAME_KEY)));
    }

    @Test
    public void moduleBeanHasCorrectUrl()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasUrlValue(EXPECTED_IFRAME_URL)));
    }

    @Test
    public void moduleBeanHasCorrectWeight()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasWeightValue(WEIGHT)));
    }

    @Test
    public void moduleBeanHasCorrectLocation()
    {
        verify(webItemModuleDescriptorFactory, times(1)).createModuleDescriptor(eq(plugin), eq(bundleContext), argThat(hasLocationValue(EXPECTED_LOCATION)));
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectLocalUrl()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameProjectConfigTabServletDescriptor(eq(plugin), any(WebItemModuleBean.class),
                eq(EXPECTED_IFRAME_DESCRIPTOR_URL), anyString(), anyString(), anyString(), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectAddonUrl()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameProjectConfigTabServletDescriptor(eq(plugin), any(WebItemModuleBean.class),
                anyString(), eq(ADDON_URL), anyString(), anyString(), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectDecorator()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameProjectConfigTabServletDescriptor(eq(plugin), any(WebItemModuleBean.class),
                anyString(), anyString(), eq(EXPECTED_DECORATOR), anyString(), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectTemplateSuffix()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameProjectConfigTabServletDescriptor(eq(plugin), any(WebItemModuleBean.class),
                anyString(), anyString(), anyString(), eq(EXPECTED_TEMPLATE_SUFFIX), any(Condition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectCondition()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameProjectConfigTabServletDescriptor(eq(plugin), any(WebItemModuleBean.class),
                anyString(), anyString(), anyString(), anyString(), any(IsProjectAdminCondition.class), anyMap());
    }

    @Test
    public void callsServletDescriptorFactoryWithCorrectTemplateMetaTags()
    {
        verify(servletDescriptorFactory, times(1)).createIFrameProjectConfigTabServletDescriptor(eq(plugin), any(WebItemModuleBean.class),
                anyString(), anyString(), anyString(), anyString(), any(Condition.class), eq(EXPECTED_META_TAGS));
    }

    private ConnectProjectAdminTabPanelModuleBean createModuleBean()
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
        return projectAdminTabPanelModuleProvider.provideModules(plugin, bundleContext, JSON_FIELD_NAME, ImmutableList.of(bean));
    }

}
