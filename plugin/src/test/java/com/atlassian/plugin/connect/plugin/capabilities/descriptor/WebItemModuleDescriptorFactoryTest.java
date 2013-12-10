package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetBean.newWebItemTargetBean;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class WebItemModuleDescriptorFactoryTest
{
    @Mock
    private WebInterfaceManager webInterfaceManager;
    @Mock
    private WebFragmentHelper webFragmentHelper;
    @Mock
    private HttpServletRequest servletRequest;

    private PluginForTests plugin;
    private WebItemModuleDescriptorFactory webItemFactory;

    @Before
    public void setup() throws ConditionLoadingException
    {
        plugin = new PluginForTests("my-key", "My Plugin");

        ConditionModuleFragmentFactory conditionModuleFragmentFactory = new ConditionModuleFragmentFactory(mock(ProductAccessor.class), new ParamsModuleFragmentFactory());
        webItemFactory = new WebItemModuleDescriptorFactory(new WebItemModuleDescriptorFactoryForTests(webInterfaceManager), new IconModuleFragmentFactory(new RemotablePluginAccessorFactoryForTests()), conditionModuleFragmentFactory);

        when(servletRequest.getContextPath()).thenReturn("http://ondemand.com/jira");

        when(webInterfaceManager.getWebFragmentHelper()).thenReturn(webFragmentHelper);

        when(webFragmentHelper.renderVelocityFragment(anyString(), anyMap())).thenAnswer(
                new Answer<Object>()
                {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        Object[] args = invocationOnMock.getArguments();
                        return (String) args[0];
                    }
                }
        );

        when(webFragmentHelper.loadCondition(anyString(), any(Plugin.class))).thenReturn(new DynamicMarkerCondition());
    }

    @Test
    public void completeKeyIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertThat(descriptor.getCompleteKey(), is("my-key:my-web-item"));
    }

    @Test
    public void sectionIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertThat(descriptor.getSection(), is("atl.admin/menu"));
    }

    @Test
    public void urlPrefixIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

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
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertThat(descriptor.getWeight(), is(123));
    }

    @Test
    public void iconIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertNull(descriptor.getIcon());
    }

    @Test
    public void styleClassIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertEquals("", descriptor.getStyleClass());
    }

    @Test
    public void styleClassIsCorrectWithNoTarget()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertEquals("", descriptor.getStyleClass());
    }

    @Test
    public void styleClassIsCorrectWithDialogTarget()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertEquals("ap-dialog", descriptor.getStyleClass());
    }

    @Test
    public void styleClassIsCorrectWithInlineDialogTarget()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertEquals("ap-inline-dialog", descriptor.getStyleClass());
    }

    @Test
    public void styleClassIsCorrectWithStylesAndInlineDialogTarget()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withStyleClasses("batman", "robin")
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertThat(descriptor.getStyleClass(), containsString("batman"));
        assertThat(descriptor.getStyleClass(), containsString("robin"));
        assertThat(descriptor.getStyleClass(), containsString("ap-inline-dialog"));
    }

    private WebItemModuleBeanBuilder createWebItemBeanBuilder()
    {
        return newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("http://www.google.com?my_project_id=${project.id}&my_project_key=${project.key}")
                .withLocation("atl.admin/menu")
                .withWeight(123);
    }
}
