package com.atlassian.plugin.connect.test.plugin.module.webitem;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.webitem.JiraWebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.product;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraWebItemModuleDescriptorFactoryTest
{
    @Mock
    private WebInterfaceManager webInterfaceManager;

    @Mock
    private WebFragmentHelper webFragmentHelper;

    @Mock
    private ContextMapURLSerializer contextMapURLSerializer;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Mock
    private RemotablePluginAccessor remotablePluginAccessor;

    private WebItemModuleDescriptor descriptor;

    @Before
    public void setup() throws ConditionLoadingException
    {
        Plugin plugin = new PluginForTests("my-key", "My Plugin");

        JiraWebItemModuleDescriptorFactory webItemFactory = new JiraWebItemModuleDescriptorFactory(
                webFragmentHelper, webInterfaceManager, new UrlVariableSubstitutor(), contextMapURLSerializer,
                jiraAuthenticationContext);

        when(servletRequest.getContextPath()).thenReturn("ElContexto");
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
        assertThat(descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()), is("ElContexto/myplugin?my_project_id=&my_project_key="));
    }


}
