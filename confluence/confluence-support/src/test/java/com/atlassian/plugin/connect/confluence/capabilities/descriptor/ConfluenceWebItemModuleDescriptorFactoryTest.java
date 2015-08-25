package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.module.webitem.WebItemModuleDescriptorData;
import com.atlassian.plugin.connect.api.module.webitem.WebLinkFactory;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.util.fixture.PluginForTests;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.product;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@Ignore ("convert to wired test")
@RunWith (MockitoJUnitRunner.class)
public class ConfluenceWebItemModuleDescriptorFactoryTest
{
    @Mock
    private WebInterfaceManager webInterfaceManager;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private ContainerContext containerContext;

    @Mock
    private WebLinkFactory webLinkFactory;

    private WebItemModuleDescriptor descriptor;

    @Before
    public void setup() throws ConditionLoadingException
    {
        Plugin plugin = new PluginForTests("my-key", "My Plugin");

        ConfluenceWebItemModuleDescriptorFactory webItemFactory =
                new ConfluenceWebItemModuleDescriptorFactory(webLinkFactory);

        when(servletRequest.getContextPath()).thenReturn("ElContexto");

        ContainerManager.getInstance().setContainerContext(containerContext);
        when(containerContext.getComponent("webInterfaceManager")).thenReturn(webInterfaceManager);

        descriptor = webItemFactory.createWebItemModuleDescriptor(WebItemModuleDescriptorData.builder()
                .setUrl("/myplugin?my_project_id={project.id}&my_project_key={project.key}")
                .setPluginKey("my-key")
                .setModuleKey("myLinkId")
                .setAbsolute(false)
                .setAddOnUrlContext(product)
                .setIsDialog(false)
                .setSection("section")
                .build());

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
