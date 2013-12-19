package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.ContentEntityForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class AbstractContentMacroTest<B extends BaseContentMacroModuleBean, M extends AbstractContentMacro, T extends BaseContentMacroModuleBeanBuilder<T, B>>
{
    public static final String PAGE_TYPE = "page";
    public static final String PAGE_ID = "56789";
    public static final String PAGE_TITLE = "Test Page";
    public static final String PAGE_VERSION = "8";
    public static final String SPACE_KEY = "space";
    public static final String SPACE_ID = "893646";
    public static final String USER_ID = "admin";
    public static final String USER_KEY = "f80808143087d180143087d3a910004";
    public static final String OUTPUT_TYPE = "display";

    @Mock
    protected UserManager userManager;
    @Mock
    protected ConversionContext conversionContext;

    protected RemotablePluginAccessorFactory remotablePluginAccessorFactory = new RemotablePluginAccessorFactoryForTests();
    protected UrlVariableSubstitutor urlVariableSubstitutor = new UrlVariableSubstitutor();


    protected abstract T createBeanBuilder();

    protected abstract M createMacro(B bean);

    protected abstract void verifyRendererInvokedWithQueryParameter(String name, String value) throws Exception;


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
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?page_type=${page.type}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("page_type", PAGE_TYPE);
    }

    @Test
    public void pageIdIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?page_id=${page.id}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("page_id", PAGE_ID);
    }

    @Test
    public void pageTitleIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?page_title=${page.title}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("page_title", PAGE_TITLE);
    }

    @Test
    public void pageVersionIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?page_version=${page.version.id}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("page_version", PAGE_VERSION);
    }

    @Test
    public void spaceKeyIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?space_key=${space.key}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("space_key", SPACE_KEY);
    }

    @Test
    public void spaceIdIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?space_id=${space.id}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("space_id", SPACE_ID);
    }

    @Test
    public void userIdIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?user_id=${user.id}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("user_id", USER_ID);
    }

    @Test
    public void userKeyIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?user_key=${user.key}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("user_key", USER_KEY);
    }

    @Test
    public void outputTypeIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?ot=${output.type}")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("ot", OUTPUT_TYPE);
    }

    @Test
    public void bodyIsPresent() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer")
                .build();

        M macro = createMacro(bean);
        macro.execute(newHashMap(), "some macro content", conversionContext);

        verifyRendererInvokedWithQueryParameter("body", "some macro content");
    }

    private Map<String, String> newHashMap()
    {
        return Maps.newHashMap();
    }
}
