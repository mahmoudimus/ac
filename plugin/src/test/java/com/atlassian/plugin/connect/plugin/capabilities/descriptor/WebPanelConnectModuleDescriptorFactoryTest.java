package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.capabilities.util.TestContextBuilder;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.extractor.ProjectContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.sal.api.user.UserManager;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean.newWebPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.TestMatchers.hasIFramePath;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebPanelConnectModuleDescriptorFactoryTest
{
    private interface PluginForTests extends Plugin, ContainerManagedPlugin {}

    private WebPanelModuleDescriptor descriptor;

    @Mock private PluginForTests plugin;
    @Mock private ConnectAutowireUtil connectAutowireUtil;
    @Mock private HostContainer hostContainer;
    @Mock private WebInterfaceManager webInterfaceManager;
    @Mock private UserManager userManager;
    @Mock private IFrameRenderer iFrameRenderer;
    @Mock private UrlValidator urlValidator;

    @Before
    public void beforeEachTest()
    {
        WebPanelConnectModuleDescriptorFactory webPanelFactory = new WebPanelConnectModuleDescriptorFactory(connectAutowireUtil);
        when(plugin.getKey()).thenReturn("my-plugin");
        when(plugin.getName()).thenReturn("My Plugin");
        ContextMapURLSerializer contextMapURLSerializer = new ContextMapURLSerializer(Arrays.asList((ContextMapParameterExtractor) new ProjectContextMapParameterExtractor(new ProjectSerializer())));
        WebPanelConnectModuleDescriptor aDescriptor = new WebPanelConnectModuleDescriptor(hostContainer, webInterfaceManager, iFrameRenderer, contextMapURLSerializer, userManager, urlValidator);
        when(connectAutowireUtil.createBean(WebPanelConnectModuleDescriptor.class)).thenReturn(aDescriptor);

        WebPanelCapabilityBean bean = newWebPanelBean()
                .withName(new I18nProperty("My Web Panel", "my.webpanel"))
                .withUrl("http://www.google.com?my_project_id=${project.id}&my_project_key=${project.key}")
                .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                .withLayout(new WebPanelLayout("10px", "100%"))
                .withWeight(50)
                .build();

        descriptor = webPanelFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();
    }

    // TODO: test condition when JD has added conditions support to capabilities

    @Test
    public void keyIsCorrect() throws Exception
    {
        assertThat(descriptor.getKey(), is("remote-web-panel-my-web-panel"));
    }

    @Test
    public void completeKeyIsCorrect() throws Exception
    {
        assertThat(descriptor.getCompleteKey(), is("my-plugin:remote-web-panel-my-web-panel"));
    }

    @Test
    public void locationIsCorrect()
    {
        assertThat(descriptor.getLocation(), is("com.atlassian.jira.plugin.headernav.left.context"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void widthIsCorrect() throws IOException
    {
        descriptor.getModule().getHtml(Collections.<String, Object>emptyMap());
        verify(iFrameRenderer).render(argThat(hasIFrameWidth("10px")), anyString(), anyMap(), anyString(), anyMap());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void heightIsCorrect() throws IOException
    {
        descriptor.getModule().getHtml(Collections.<String, Object>emptyMap());
        verify(iFrameRenderer).render(argThat(hasIFrameHeight("100%")), anyString(), anyMap(), anyString(), anyMap());
    }

    @Test
    public void weightIsCorrect()
    {
        assertThat(descriptor.getWeight(), is(50));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void urlIsCorrectWhenContextIsEmpty() throws IOException
    {
        descriptor.getModule().getHtml(Collections.<String, Object>emptyMap());
        verify(iFrameRenderer).render(argThat(hasIFramePath("http://www.google.com?my_project_id=&amp;my_project_key=")), anyString(), anyMap(), anyString(), anyMap());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void urlIsCorrectWhenContextIsPopulated() throws IOException
    {
        descriptor.getModule().getHtml(TestContextBuilder.buildContextMap());
        verify(iFrameRenderer).render(argThat(hasIFramePath(String.format("http://www.google.com?my_project_id=%d&amp;my_project_key=%s", TestContextBuilder.PROJECT_ID, TestContextBuilder.PROJECT_KEY))), anyString(), anyMap(), anyString(), anyMap());
    }

    @Test
    public void i18nKeyIsCorrect()
    {
        assertThat(descriptor.getI18nNameKey(), is("my.webpanel"));
    }

    private static ArgumentMatcher<IFrameContext> hasIFrameWidth(String width)
    {
        return hasIFrameParam("width", width);
    }

    private static ArgumentMatcher<IFrameContext> hasIFrameHeight(String height)
    {
        return hasIFrameParam("height", height);
    }

    private static ArgumentMatcher<IFrameContext> hasIFrameParam(String name, String expectedValue)
    {
        return new IFrameParamMatcher(name, expectedValue);
    }

    private static class IFrameParamMatcher extends ArgumentMatcher<IFrameContext>
    {
        private final String name;
        private final String expectedValue;

        private IFrameParamMatcher(String name, String expectedValue)
        {
            this.name = checkNotNull(name);
            this.expectedValue = checkNotNull(expectedValue);
        }

        @Override
        public boolean matches(Object argument)
        {
            assertThat(argument, is(instanceOf(IFrameContext.class)));
            IFrameContext iFrameContext = (IFrameContext) argument;
            return expectedValue.equals(iFrameContext.getIFrameParams().getAsMap().get(name));
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("IFrameContext with param ");
            description.appendValue(name);
            description.appendText(" = ");
            description.appendValue(expectedValue);
        }
    }
}
