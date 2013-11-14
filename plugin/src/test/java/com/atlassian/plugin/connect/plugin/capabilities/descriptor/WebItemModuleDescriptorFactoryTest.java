package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.descriptor.WebItemModuleDescriptorFactoryForTests;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebItemModuleDescriptorFactoryTest
{
    @Mock private WebInterfaceManager webInterfaceManager;
    @Mock private WebFragmentHelper webFragmentHelper;
    @Mock private HttpServletRequest servletRequest;
    private WebItemModuleDescriptor descriptor;

    @Before
    public void setup() throws ConditionLoadingException
    {
        Plugin plugin = new PluginForTests("my-key", "My Plugin");

        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();
        ConditionModuleFragmentFactory conditionModuleFragmentFactory = new ConditionModuleFragmentFactory(mock(ProductAccessor.class),new ParamsModuleFragmentFactory());

        WebItemModuleDescriptorFactory webItemFactory = new WebItemModuleDescriptorFactory(new WebItemModuleDescriptorFactoryForTests(webInterfaceManager), new IconModuleFragmentFactory(new RemotablePluginAccessorFactoryForTests()), conditionModuleFragmentFactory);

        when(servletRequest.getContextPath()).thenReturn("http://ondemand.com/jira");
        
        when(webInterfaceManager.getWebFragmentHelper()).thenReturn(webFragmentHelper);

        when(webFragmentHelper.renderVelocityFragment(anyString(),anyMap())).thenAnswer(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        Object[] args = invocationOnMock.getArguments();
                        return (String) args[0];
                    }
                }
        );

        when(webFragmentHelper.loadCondition(anyString(), any(Plugin.class))).thenReturn(new DynamicMarkerCondition());

        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("http://www.google.com?my_project_id=${project.id}&my_project_key=${project.key}")
                .withLocation("atl.admin/menu")
                .withWeight(123)
                .build();

        this.descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        this.descriptor.enabled();
    }
    
    @Test
    public void completeKeyIsCorrect()
    {
        assertThat(descriptor.getCompleteKey(), is("my-key:my-web-item"));
    }

    @Test
    public void sectionIsCorrect()
    {
        assertThat(descriptor.getSection(), is("atl.admin/menu"));
    }

    @Test
    public void urlPrefixIsCorrect()
    {
        assertThat(descriptor.getLink().getDisplayableUrl(mock(HttpServletRequest.class), new HashMap<String, Object>()), startsWith("http://www.google.com"));
    }

    /*

    TODO in ACDEV-494: comment in the following 2 tests and fix URL variable substitution.

    @Test
    public void urlIsCorrectWhenThereIsNoContext()
    {
        assertThat(descriptor.getLink().getDisplayableUrl(mock(HttpServletRequest.class), new HashMap<String, Object>()), is("http://www.google.com?my_project_id=&my_project_key="));
    }

    @Test
    public void urlIsCorrectWhenThereIsContext()
    {
        assertThat(descriptor.getLink().getDisplayableUrl(mock(HttpServletRequest.class), TestContextBuilder.buildContextMap()), is(String.format("http://www.google.com?my_project_id=%d&my_project_key=%s", TestContextBuilder.PROJECT_ID, TestContextBuilder.PROJECT_KEY)));
    }
    */

    @Test
    public void weightIsCorrect()
    {
        assertThat(descriptor.getWeight(), is(123));
    }

    @Test
    public void iconIsCorrect()
    {
        assertNull(descriptor.getIcon());
    }

    @Test
    public void styleClassIsCorrect()
    {
        assertEquals("",descriptor.getStyleClass());
    }
}
