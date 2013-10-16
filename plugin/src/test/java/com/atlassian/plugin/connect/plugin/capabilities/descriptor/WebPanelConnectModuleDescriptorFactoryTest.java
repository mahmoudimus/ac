package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelLayout;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
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

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean.newWebPanelBean;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebPanelConnectModuleDescriptorFactoryTest
{
    private interface PluginForTests extends Plugin, ContainerManagedPlugin {}

    @Mock private PluginForTests plugin;
    @Mock private ContainerAccessor containerAccessor;
    @Mock private WebPanelModuleDescriptor descriptor;

    @Before
    public void beforeEachTest()
    {
        WebPanelConnectModuleDescriptorFactory webPanelFactory = new WebPanelConnectModuleDescriptorFactory();
        when(plugin.getKey()).thenReturn("my-key");
        when(plugin.getName()).thenReturn("My Plugin");
        when(plugin.getContainerAccessor()).thenReturn(containerAccessor);
        when(containerAccessor.createBean(WebPanelModuleDescriptor.class)).thenReturn(descriptor);

        WebPanelCapabilityBean bean = newWebPanelBean()
                .withName(new I18nProperty("My Web Panel", "my.webpanel"))
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .withLayout(new WebPanelLayout("10px", "100%"))
                .withWeight(50)
                .build();

        webPanelFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
    }

    // TODO: test condition when JD has added conditions support to capabilities

    @Test
    public void keyIsCorrect() throws Exception
    {
        verify(descriptor).init(eq(plugin), argThat(hasAttribute("key", withValue("my-web-panel"))));
    }

    @Test
    public void locationIsCorrect()
    {
        verify(descriptor).init(eq(plugin), argThat(hasAttribute("location", withValue("atl.admin/menu"))));
    }

    @Test
    public void widthIsCorrect()
    {
        verify(descriptor).init(eq(plugin), argThat(hasAttribute("width", withValue("10px"))));
    }

    @Test
    public void heightIsCorrect()
    {
        verify(descriptor).init(eq(plugin), argThat(hasAttribute("height", withValue("100%"))));
    }

    @Test
    public void weightIsCorrect()
    {
        verify(descriptor).init(eq(plugin), argThat(hasAttribute("weight", withValue("50"))));
    }

    @Test
    public void urlIsCorrect()
    {
        verify(descriptor).init(eq(plugin), argThat(hasAttribute("url", withValue("http://www.google.com"))));
    }

    @Test
    public void isEnabled()
    {
        verify(descriptor).init(eq(plugin), argThat(hasAttribute("state", withValue("enabled"))));
    }

    @Test
    public void i18nKeyIsCorrect()
    {
        verify(descriptor).init(eq(plugin), argThat(hasElement("label", withAttribute("key", withValue("my.webpanel")))));
    }

    @Test
    public void i18TextIsCorrect()
    {
        verify(descriptor).init(eq(plugin), argThat(hasElement("label", withText("My Web Panel"))));
    }

    @Test
    public void isSystem()
    {
        verify(descriptor).init(eq(plugin), argThat(hasAttribute("system", withValue("true"))));
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
