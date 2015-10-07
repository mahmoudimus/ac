package com.atlassian.plugin.connect.jira.capabilities.descriptor;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.module.webitem.WebItemModuleDescriptorData;
import com.atlassian.plugin.connect.api.module.webitem.WebLinkFactory;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.module.webitem.WebLinkFactoryImpl;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.util.fixture.PluginForTests;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.product;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith (MockitoJUnitRunner.class)
public class JiraWebItemModuleDescriptorFactoryTest
{
    @Mock
    private WebInterfaceManager webInterfaceManager;

    @Mock
    private WebFragmentHelper webFragmentHelper;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private IFrameUriBuilderFactory iFrameUriBuilderFactory;

    @Mock
    private PluggableParametersExtractor webFragmentModuleContextExtractor;

    @Mock
    private ModuleContextFilter moduleContextFilter;

    private JiraWebItemModuleDescriptorFactory webItemFactory;

    private Plugin plugin;

    @Before
    public void setup() throws ConditionLoadingException
    {
        plugin = new PluginForTests("my-key", "My Plugin");

        UrlVariableSubstitutor urlVariableSubstitutor = createUrlSubstitutor();

        WebLinkFactory webLinkFactory = new WebLinkFactoryImpl(webFragmentModuleContextExtractor, iFrameUriBuilderFactory, webFragmentHelper, urlVariableSubstitutor, moduleContextFilter);
        webItemFactory = new JiraWebItemModuleDescriptorFactory(webInterfaceManager, jiraAuthenticationContext, webLinkFactory);

        when(servletRequest.getContextPath()).thenReturn("ElContexto");

        ModuleContextParameters contextParameters = mock(ModuleContextParameters.class);
        when(contextParameters.isEmpty()).thenReturn(true);
        when(moduleContextFilter.filter(any(ModuleContextParameters.class))).thenReturn(contextParameters);
    }

    @Test
    public void urlPrefixIsCorrect()
    {
        WebItemModuleDescriptor descriptor = webItemFactory.createWebItemModuleDescriptor(WebItemModuleDescriptorData.builder()
                .setUrl("/myplugin?my_project_id")
                .setPluginKey("my-key")
                .setModuleKey("myLinkId")
                .setAbsolute(false)
                .setAddOnUrlContext(product)
                .setIsDialog(false)
                .setSection("section")
                .build());

        descriptor.init(plugin, createElement());
        descriptor.enabled();

        String url = descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>());
        assertThat(url, is("ElContexto/myplugin?my_project_id"));
    }

    @Test
    public void urlIsCorrectWhenThereIsNoContext()
    {
        WebItemModuleDescriptor descriptor = webItemFactory.createWebItemModuleDescriptor(
                WebItemModuleDescriptorData.builder()
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

        String url = descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>());
        assertThat(url, is("ElContexto/myplugin?my_project_id=&my_project_key="));
    }

    @Test
    public void testWebItemLinkContainsAllQueryParams() throws Exception
    {
        final ImmutableSet<String> ADMIN_MENUS_KEYS = ImmutableSet.of(
                "admin_system_menu",
                "admin_plugins_menu",
                "admin_users_menu",
                "admin_issues_menu",
                "admin_project_menu");

        for (String key : ADMIN_MENUS_KEYS)
        {
            testWebItemLinkContainsAllQueryParamsForSection(key);
        }
    }

    @Test
    public void testWebItemLinkQueryParamIsNotOverridenBySourceParamIfPresent()
    {
        String moduleKey = "myLinkId";
        WebItemModuleDescriptor descriptor = webItemFactory.createWebItemModuleDescriptor(WebItemModuleDescriptorData.builder()
                        .setUrl("/myplugin?s=blabla")
                        .setPluginKey("my-key")
                        .setModuleKey(moduleKey)
                        .setAbsolute(false)
                        .setAddOnUrlContext(AddOnUrlContext.page)
                        .setIsDialog(false)
                        .setSection("admin_system_menu")
                        .build());

        descriptor.init(plugin, createElement());
        descriptor.enabled();

        String displayableUrl = descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>());

        assertThat(displayableUrl, urlHasWebItemSourceQueryParameter("blabla"));
    }

    private void testWebItemLinkContainsAllQueryParamsForSection(String section)
    {
        String moduleKey = "myLinkId";
        WebItemModuleDescriptor descriptor = webItemFactory.createWebItemModuleDescriptor(WebItemModuleDescriptorData.builder()
                        .setUrl("/myplugin")
                        .setPluginKey("my-key")
                        .setModuleKey(moduleKey)
                        .setAbsolute(false)
                        .setAddOnUrlContext(AddOnUrlContext.page)
                        .setIsDialog(false)
                        .setSection(section)
                        .build());

        descriptor.init(plugin, createElement());
        descriptor.enabled();

        String displayableUrl = descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>());

        assertThat(displayableUrl, urlHasWebItemSourceQueryParameter(moduleKey));
    }

    private UrlVariableSubstitutor createUrlSubstitutor()
    {
        UrlVariableSubstitutor mock = mock(UrlVariableSubstitutor.class);
        when(mock.append(anyString(), anyMap())).then(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                return invocationOnMock.getArguments()[0];
            }
        });
        when(mock.replace(anyString(), anyMap())).then(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                String template = (String) invocationOnMock.getArguments()[0];
                return template.replaceAll("\\{.*?\\}", "");
            }
        });
        return mock;
    }

    private Element createElement()
    {
        DOMElement element = new DOMElement("web-item");
        element.addAttribute("key", "mykey");
        return element;
    }

    private Matcher<String> urlHasWebItemSourceQueryParameter(final String value)
    {
        return new TypeSafeMatcher<String>()
        {
            @Override
            protected boolean matchesSafely(String url)
            {
                return url.contains(JiraWebItemModuleDescriptorFactory.WEB_ITEM_SOURCE_QUERY_PARAM + "=" + value);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Url containing " + JiraWebItemModuleDescriptorFactory.WEB_ITEM_SOURCE_QUERY_PARAM + "=" + value);
            }
        };
    }
}
