package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.spring.container.ContainerContext;
import com.atlassian.spring.container.ContainerManager;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext.product;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfluenceWebItemModuleDescriptorFactoryTest
{
    @Mock
    private WebInterfaceManager webInterfaceManager;

    @Mock
    private WebFragmentHelper webFragmentHelper;

    @Mock
    private ContextMapURLSerializer contextMapURLSerializer;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private ContainerContext containerContext;

    @Mock
    private RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Mock
    private RemotablePluginAccessor remotablePluginAccessor;

    private WebItemModuleDescriptor descriptor;

    @Before
    public void setup() throws ConditionLoadingException
    {
        Plugin plugin = new PluginForTests("my-key", "My Plugin");

        ConfluenceWebItemModuleDescriptorFactory webItemFactory = new ConfluenceWebItemModuleDescriptorFactory(
                new UrlVariableSubstitutor(), contextMapURLSerializer, webFragmentHelper);

        when(servletRequest.getContextPath()).thenReturn("ElContexto");

        ContainerManager.getInstance().setContainerContext(containerContext);
        when(containerContext.getComponent("webInterfaceManager")).thenReturn(webInterfaceManager);
        when(contextMapURLSerializer.getExtractedWebPanelParameters(anyMap())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return invocation.getArguments()[0];
            }
        });
        when(remotablePluginAccessorFactory.get(anyString())).thenReturn(remotablePluginAccessor);
        descriptor = webItemFactory.createWebItemModuleDescriptor(
                "/myplugin?my_project_id={project.id}&my_project_key={project.key}",
                "myLinkId", false, product, remotablePluginAccessorFactory.get(plugin.getKey()));
        descriptor.init(plugin, createElement());
        descriptor.enabled();
    }

    private Element createElement()
    {
        DOMElement element = new DOMElement("web-item");
        element.addAttribute("key", "mykey");
        return element;
    }

    @Test
    public void urlPrefixIsCorrect()
    {
        assertThat(descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()), startsWith("ElContexto"));
    }

    @Test
    public void urlIsCorrectWhenThereIsNoContext()
    {
        assertThat(descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()),
                is("ElContexto/myplugin?my_project_id=&my_project_key="));
    }

    @Test
    public void urlIsCorrectWhenThereIsAContext()
    {
        assertThat(descriptor.getLink().getDisplayableUrl(servletRequest, ImmutableMap.<String, Object>of("project",
                ImmutableMap.<String, Object>of("key", "FOO", "id", "10"))),
                is("ElContexto/myplugin?my_project_id=10&my_project_key=FOO"));
    }
}
