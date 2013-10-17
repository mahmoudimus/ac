package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelLayout;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Objects;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.Collections;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean.newWebPanelBean;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebPanelConnectModuleDescriptorFactoryTest
{
    private interface PluginForTests extends Plugin, ContainerManagedPlugin {}

    private WebPanelModuleDescriptor descriptor;

    @Mock private PluginForTests plugin;
    @Mock private ContainerAccessor containerAccessor;
    @Mock private WebInterfaceManager webInterfaceManager;
    @Mock private UserManager userManager;
    @Mock private ContextMapURLSerializer contextMapURLSerializer;
    @Mock private IFrameRenderer iFrameRenderer;
    @Mock private UrlValidator urlValidator;

    @Before
    public void beforeEachTest()
    {
        WebPanelConnectModuleDescriptorFactory webPanelFactory = new WebPanelConnectModuleDescriptorFactory();
        when(plugin.getKey()).thenReturn("my-plugin");
        when(plugin.getName()).thenReturn("My Plugin");
        when(plugin.getContainerAccessor()).thenReturn(containerAccessor);
        when(containerAccessor.createBean(WebInterfaceManager.class)).thenReturn(webInterfaceManager);
        when(containerAccessor.createBean(UserManager.class)).thenReturn(userManager);
        when(containerAccessor.createBean(ContextMapURLSerializer.class)).thenReturn(contextMapURLSerializer);
        when(containerAccessor.createBean(IFrameRenderer.class)).thenReturn(iFrameRenderer);
        when(containerAccessor.createBean(UrlValidator.class)).thenReturn(urlValidator);

        WebPanelCapabilityBean bean = newWebPanelBean()
                .withName(new I18nProperty("My Web Panel", "my.webpanel"))
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
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
        assertThat(descriptor.getKey(), is("my-web-panel"));
    }

    @Test
    public void completeKeyIsCorrect() throws Exception
    {
        assertThat(descriptor.getCompleteKey(), is("my-plugin:my-web-panel"));
    }

    @Test
    public void locationIsCorrect()
    {
        assertThat(descriptor.getLocation(), is("atl.admin/menu"));
    }

    @Test
    public void widthIsCorrect() throws IOException
    {
        descriptor.getModule().getHtml(Collections.<String, Object>emptyMap());
        verify(iFrameRenderer).render(argThat(hasIFrameWidth("10px")), anyString(), anyMap(), anyString(), anyMap());
    }

    @Test
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
    public void urlIsCorrect() throws IOException
    {
        descriptor.getModule().getHtml(Collections.<String, Object>emptyMap());
        verify(iFrameRenderer).render(argThat(hasIFramePath("http://www.google.com")), anyString(), anyMap(), anyString(), anyMap());
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

    private static ArgumentMatcher<IFrameContext> hasIFramePath(final String url)
    {
        assertThat(url, is(not(nullValue())));

        return new ArgumentMatcher<IFrameContext>()
        {
            @Override
            public boolean matches(Object argument)
            {
                assertThat(argument, is(instanceOf(IFrameContext.class)));
                IFrameContext iFrameContext = (IFrameContext) argument;
                return url.equals(iFrameContext.getIframePath());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("IFrameContext with iFrame URL ");
                description.appendValue(url);
            }
        };
    }

    private static ArgumentMatcher<Element> hasElement(String name, ArgumentMatcher<Element> expectedValueMatcher)
    {
        return new SubElementMatcher(name, expectedValueMatcher);
    }

    private static ArgumentMatcher<Element> hasAttribute(String name, ArgumentMatcher<Attribute> expectedValueMatcher)
    {
        return withAttribute(name, expectedValueMatcher); // just a synonym to make test assertions read as natural English
    }

    private static ArgumentMatcher<Element> withAttribute(String name, ArgumentMatcher<Attribute> expectedValueMatcher)
    {
        return new ElementAttributeMatcher(name, expectedValueMatcher);
    }

    private static ArgumentMatcher<Attribute> withValue(String expectedValue)
    {
        return new AttributeValueMatcher(expectedValue);
    }

    private static ArgumentMatcher<Element> withText(String expectedValue)
    {
        return new ElementTextMatcher(expectedValue);
    }

    private static class AttributeValueMatcher extends ArgumentMatcher<Attribute>
    {
        private final String expectedValue;

        private AttributeValueMatcher(String expectedValue)
        {
            this.expectedValue = checkNotNull(expectedValue);
        }

        @Override
        public boolean matches(Object argument)
        {
            assertThat(argument, is(instanceOf(Attribute.class)));
            return Objects.equal(expectedValue, ((Attribute)argument).getValue());
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Attribute with value ");
            description.appendValue(expectedValue);
        }
    }

    private static class ElementTextMatcher extends ArgumentMatcher<Element>
    {
        private final String expectedValue;

        private ElementTextMatcher(String expectedValue)
        {
            this.expectedValue = checkNotNull(expectedValue);
        }

        @Override
        public boolean matches(Object argument)
        {
            assertThat(argument, is(instanceOf(Element.class)));
            return Objects.equal(expectedValue, ((Element)argument).getText());
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Attribute with text ");
            description.appendValue(expectedValue);
        }
    }

    private static class SubElementMatcher extends ArgumentMatcher<Element>
    {
        private final String name;
        private final ArgumentMatcher<Element> expectedValueMatcher;

        private SubElementMatcher(String name, ArgumentMatcher<Element> expectedValueMatcher)
        {
            this.name = checkNotNull(name);
            this.expectedValueMatcher = checkNotNull(expectedValueMatcher);
        }

        @Override
        public boolean matches(Object argument)
        {
            assertThat(argument, is(instanceOf(Element.class)));
            return expectedValueMatcher.matches(((Element)argument).element(name));
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Element with child Element ");
            description.appendValue(name);
            description.appendText(" { ");
            expectedValueMatcher.describeTo(description);
            description.appendText(" } ");
        }
    }

    private static class ElementAttributeMatcher extends ArgumentMatcher<Element>
    {
        private final String name;
        private final ArgumentMatcher<Attribute> expectedValueMatcher;

        private ElementAttributeMatcher(String name, ArgumentMatcher<Attribute> expectedValueMatcher)
        {
            this.name = checkNotNull(name);
            this.expectedValueMatcher = checkNotNull(expectedValueMatcher);
        }

        @Override
        public boolean matches(Object argument)
        {
            assertThat(argument, is(instanceOf(Element.class)));
            return expectedValueMatcher.matches(((Element) argument).attribute(name));
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Element with attribute ");
            description.appendValue(name);
            description.appendText(" { ");
            expectedValueMatcher.describeTo(description);
            description.appendText(" } ");
        }
    }
}
