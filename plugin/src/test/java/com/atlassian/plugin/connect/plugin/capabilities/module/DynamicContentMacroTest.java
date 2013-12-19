package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.ContentEntityForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DynamicContentMacroTest
{
    private static final String PAGE_TYPE = "page";
    private static final String PAGE_ID = "56789";
    private static final String PAGE_TITLE = "Test Page";
    private static final String PAGE_VERSION = "8";
    private static final String SPACE_KEY = "space";
    private static final String SPACE_ID = "893646";
    private static final String USER_ID = "admin";
    private static final String USER_KEY = "f80808143087d180143087d3a910004";
    private static final String OUTPUT_TYPE = "display";

    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private UserManager userManager;
    @Mock
    private ConversionContext conversionContext;

    private RemotablePluginAccessorFactory remotablePluginAccessorFactory = new RemotablePluginAccessorFactoryForTests();
    private UrlVariableSubstitutor urlVariableSubstitutor = new UrlVariableSubstitutor();


    @Before
    public void beforeEachTest()
    {
        ContentEntityForTests contentEntity = new ContentEntityForTests(PAGE_TYPE, PAGE_ID, PAGE_TITLE, PAGE_VERSION, SPACE_KEY, SPACE_ID);
        UserProfile user = mock(UserProfile.class);
        UserKey userKey = new UserKey(USER_KEY);

        when(conversionContext.getEntity()).thenReturn(contentEntity);
        when(conversionContext.getOutputType()).thenReturn(OUTPUT_TYPE);
        when(userManager.getRemoteUser()).thenReturn(user);

        when(user.getUsername()).thenReturn(USER_ID);
        when(user.getUserKey()).thenReturn(userKey);
    }

    @Test
    public void pageTypeIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?page_type=${page.type}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("page_type", PAGE_TYPE);
    }

    @Test
    public void pageIdIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?page_id=${page.id}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("page_id", PAGE_ID);
    }

    @Test
    public void pageTitleIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?page_title=${page.title}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("page_title", PAGE_TITLE);
    }

    @Test
    public void pageVersionIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?page_version=${page.version.id}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("page_version", PAGE_VERSION);
    }

    @Test
    public void spaceKeyIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?space_key=${space.key}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("space_key", SPACE_KEY);
    }

    @Test
    public void spaceIdIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?space_id=${space.id}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("space_id", SPACE_ID);
    }

    @Test
    public void userIdIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?user_id=${user.id}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("user_id", USER_ID);
    }

    @Test
    public void userKeyIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?user_key=${user.key}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("user_key", USER_KEY);
    }

    @Test
    public void outputTypeIsReplaced() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer?ot=${output.type}")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("ot", OUTPUT_TYPE);
    }

    @Test
    public void bodyIsPresent() throws Exception
    {
        DynamicContentMacroModuleBean bean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer")
                .build();

        DynamicContentMacro macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("body", "some macro content");
    }

    private void verifyRendererInvokedWithQueryParameter(String name, String value) throws Exception
    {
        verify(iFrameRenderer).render(any(IFrameContext.class), anyString(), argThat(hasQueryParam(name, value)), anyString(), anyMap());
    }

    private ArgumentMatcher<Map<String, String[]>> hasQueryParam(final String name, final String value)
    {
        return new ArgumentMatcher<Map<String, String[]>>()
        {
            @Override
            public boolean matches(Object actual)
            {
                Map<String, String[]> map = (Map<String, String[]>) actual;
                return map.containsKey(name) && map.get(name).length == 1 && value.equals(map.get(name)[0]);
            }
        };
    }

    private DynamicContentMacro createMacro(DynamicContentMacroModuleBean bean)
    {
        return new DynamicContentMacro("my-plugin", bean, userManager, iFrameRenderer, remotablePluginAccessorFactory, urlVariableSubstitutor);
    }

    private Map<String, String> newHashMap()
    {
        return Maps.newHashMap();
    }
}
