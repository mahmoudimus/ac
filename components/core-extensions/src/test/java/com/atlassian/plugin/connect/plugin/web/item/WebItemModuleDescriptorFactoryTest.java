package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.web.WebFragmentLocationQualifier;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.test.fixture.PluginForTests;
import com.atlassian.plugin.connect.test.fixture.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebIcon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class WebItemModuleDescriptorFactoryTest {
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
    private WebFragmentLocationQualifier locationQualifier;

    private PluginForTests plugin;
    private WebItemModuleDescriptorFactory webItemFactory;

    private ConnectAddonBean addon;

    @Before
    public void setup() throws ConditionLoadingException {
        plugin = new PluginForTests("my-key", "My Plugin");
        this.addon = newConnectAddonBean().withKey("my-key").build();


        RemotablePluginAccessorFactoryForTests pluginAccessorFactory = new RemotablePluginAccessorFactoryForTests();
        pluginAccessorFactory.withBaseUrl(ADDON_BASE_URL);
        webItemFactory = new WebItemModuleDescriptorFactoryImpl(new WebItemModuleDescriptorFactoryForTests(webInterfaceManager),
                new IconModuleFragmentFactory(pluginAccessorFactory), locationQualifier, conditionModuleFragmentFactory
        );

        when(locationQualifier.processLocation(any(String.class), any(ConnectAddonBean.class))).then((invocation) -> invocation.getArguments()[0]);

        when(servletRequest.getContextPath()).thenReturn("http://ondemand.com/jira");

        when(webInterfaceManager.getWebFragmentHelper()).thenReturn(webFragmentHelper);

        when(webFragmentHelper.renderVelocityFragment(anyString(), anyMapOf(String.class, Object.class))).thenAnswer(
                invocationOnMock -> {
                    Object[] args = invocationOnMock.getArguments();
                    return args[0];
                }
        );
    }

    @Test
    public void completeKeyIsCorrect() {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertThat(descriptor.getCompleteKey(), is("my-key:" + addonAndModuleKey("my-key", "my-web-item")));
    }

    @Test
    public void linkIdIsCorrect() {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertThat(descriptor.getLink().getId(), is(addonAndModuleKey("my-key-my-key", "my-web-item")));
    }

    @Test
    public void sectionIsCorrect() {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertThat(descriptor.getSection(), is("atl.admin/menu"));
    }

    @Test
    public void urlPrefixIsCorrect() {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertThat(descriptor.getLink().getDisplayableUrl(mock(HttpServletRequest.class), new HashMap<>()), startsWith("http://www.google.com"));
    }

    @Test
    public void weightIsCorrect() {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertThat(descriptor.getWeight(), is(123));
    }

    @Test
    public void iconIsCorrect() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withIcon(
                        newIconBean()
                                .withHeight(16)
                                .withWidth(24)
                                .withUrl("/static/images/icon.png")
                                .build()
                )
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        WebIcon icon = descriptor.getIcon();
        assertNotNull(icon);
        assertThat(icon.getHeight(), is(16));
        assertThat(icon.getWidth(), is(24));
        assertThat(icon.getUrl().getRenderedUrl(Collections.<String, Object>emptyMap()), is(ADDON_BASE_URL + "/static/images/icon.png"));
    }

    @Test
    public void styleClassIsEmptyWhenNotDefined() {
        WebItemModuleBean bean = createWebItemBeanBuilder().build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertEquals("", descriptor.getStyleClass());
    }

    @Test
    public void styleClassIsPresetWhenDefined() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withStyleClasses("batman", "robin", "mr-freeze")
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertThat(descriptor.getStyleClass(), allOf(containsString("batman"), containsString("robin"), containsString("mr-freeze")));
    }

    @Test
    public void styleClassIsCorrectWithNoTarget() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertEquals("", descriptor.getStyleClass());
    }

    @Test
    public void styleClassIsCorrectWithDialogTarget() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertTrue(descriptor.getStyleClass().contains("ap-dialog"));
    }

    @Test
    public void styleClassIsCorrectWithInlineDialogTarget() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-inline-dialog"));
    }

    @Test
    public void styleClassIsCorrectWithStylesAndInlineDialogTarget() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withStyleClasses("batman", "robin")
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();

        assertThat(descriptor.getStyleClass(), allOf(containsString("batman"), containsString("robin")));
    }

    @Test
    public void styleClassIsCorrectWithPluginKeyAndDialogTarget() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-plugin-key-" + plugin.getKey()));
    }

    @Test
    public void styleClassIsCorrectWithModuleKeyAndDialogTarget() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-module-key-" + descriptor.getKey()));
    }

    @Test
    public void styleClassIsCorrectWithPluginKeyAndInlineDialogTarget() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-plugin-key-" + plugin.getKey()));
    }

    @Test
    public void styleClassIsCorrectWithModuleKeyAndInlineDialogTarget() {
        WebItemModuleBean bean = createWebItemBeanBuilder()
                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.inlineDialog).build())
                .build();
        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        descriptor.enabled();
        assertTrue(descriptor.getStyleClass().contains("ap-module-key-" + descriptor.getKey()));
    }

    private WebItemModuleBeanBuilder createWebItemBeanBuilder() {
        return newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withKey("my-web-item")
                .withUrl("http://www.google.com?my_project_id={project.id}&my_project_key={project.key}")
                .withLocation("atl.admin/menu")
                .withWeight(123);
    }
}
