package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.web.item.ModuleLocationQualifier;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.util.fixture.PluginForTests;
import com.atlassian.plugin.connect.util.fixture.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebIcon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class WebItemModuleDescriptorFactoryTest
{
    private static final String ADDON_BASE_URL = "https://myapp.heroku.com/foo";

    @Mock
    private WebInterfaceManager webInterfaceManager;

    @Mock
    private WebFragmentHelper webFragmentHelper;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private ConditionModuleFragmentFactory conditionModuleFragmentFactory;

    @Mock
    private ConnectModuleProviderContext moduleProviderContext;

    @Mock
    private ModuleLocationQualifier locationQualifier;

    private PluginForTests plugin;
    private WebItemModuleDescriptorFactory webItemFactory;

    private ConnectAddonBean addon;

    @Before
    public void setup() throws ConditionLoadingException
    {
        plugin = new PluginForTests("my-key", "My Plugin");
        this.addon = newConnectAddonBean().withKey("my-key").build();


        RemotablePluginAccessorFactoryForTests pluginAccessorFactory = new RemotablePluginAccessorFactoryForTests();
        pluginAccessorFactory.withBaseUrl(ADDON_BASE_URL);
        webItemFactory = new WebItemModuleDescriptorFactoryImpl(new WebItemModuleDescriptorFactoryForTests(webInterfaceManager),
                new IconModuleFragmentFactory(pluginAccessorFactory), conditionModuleFragmentFactory
        );

        when(moduleProviderContext.getConnectAddonBean()).thenReturn(addon);
        when(moduleProviderContext.getLocationQualifier()).thenReturn(locationQualifier);
        when(locationQualifier.processLocation(any(String.class))).then((invocation) -> invocation.getArguments()[0]);

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
    }

    @Test
    public void completeKeyIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertThat(descriptor.getCompleteKey(), is("my-key:" + addonAndModuleKey("my-key","my-web-item")));
    }

    @Test
    public void linkIdIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertThat(descriptor.getLink().getId(), is(addonAndModuleKey("my-key-my-key","my-web-item")));
    }

    @Test
    public void sectionIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertThat(descriptor.getSection(), is("atl.admin/menu"));
    }

    @Test
    public void urlPrefixIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertThat(descriptor.getLink().getDisplayableUrl(mock(HttpServletRequest.class), new HashMap<String, Object>()), startsWith("http://www.google.com"));
    }

    @Test
    public void weightIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertThat(descriptor.getWeight(), is(123));
    }

    @Test
    public void iconIsCorrect()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withIcon(
                        newIconBean()
                                .withHeight(16)
                                .withWidth(24)
                                .withUrl("/static/images/icon.png")
                                .build()
                )
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        WebIcon icon = descriptor.getIcon();
        assertNotNull(icon);
        assertThat(icon.getHeight(), is(16));
        assertThat(icon.getWidth(), is(24));
        assertThat(icon.getUrl().getRenderedUrl(Collections.<String, Object>emptyMap()), is(ADDON_BASE_URL + "/static/images/icon.png"));
    }

    @Test
    public void styleClassIsEmptyWhenNotDefined()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertEquals("", descriptor.getStyleClass());
    }

    @Test
    public void styleClassIsPresetWhenDefined()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withStyleClasses("batman", "robin", "mr-freeze")
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertThat(descriptor.getStyleClass(), allOf(containsString("batman"), containsString("robin"), containsString("mr-freeze")));
    }

    @Test
    public void styleClassIsCorrectWithNoTarget()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertEquals("", descriptor.getStyleClass());
    }

    @Test
    public void styleClassIsCorrectWithDialogTarget()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertTrue(descriptor.getStyleClass().contains("ap-dialog"));
    }

    @Test
    public void styleClassIsCorrectWithInlineDialogTarget()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-inline-dialog"));
    }

    @Test
    public void styleClassIsCorrectWithStylesAndInlineDialogTarget()
    {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withStyleClasses("batman", "robin")
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();

        assertThat(descriptor.getStyleClass(), allOf(containsString("batman"), containsString("robin")));
    }

    @Test
    public void styleClassIsCorrectWithPluginKeyAndDialogTarget(){
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-plugin-key-" + plugin.getKey()));
    }

    @Test
    public void styleClassIsCorrectWithModuleKeyAndDialogTarget(){
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-module-key-" + descriptor.getKey()));
    }

    @Test
    public void styleClassIsCorrectWithPluginKeyAndInlineDialogTarget(){
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-plugin-key-" + plugin.getKey()));
    }

    @Test
    public void styleClassIsCorrectWithModuleKeyAndInlineDialogTarget(){
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-module-key-" + descriptor.getKey()));
    }

    private WebItemModuleBeanBuilder createWebItemBeanBuilder()
    {
        return newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withKey("my-web-item")
                .withUrl("http://www.google.com?my_project_id={project.id}&my_project_key={project.key}")
                .withLocation("atl.admin/menu")
                .withWeight(123);
    }
}