package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.service.IsDevModeServiceImpl;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.Maps;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public abstract class AbstractContentMacroUrlTemplateTest<B extends BaseContentMacroModuleBean, M extends AbstractMacro, T extends BaseContentMacroModuleBeanBuilder<T, B>>
{
    public static final String BODY = "some macro content";
    public static final String HASH = DigestUtils.md5Hex(BODY);
    public static final String PAGE_TYPE = "page";
    public static final String PAGE_ID = "56789";
    public static final String PAGE_TITLE = "Test Page";
    public static final String PAGE_VERSION = "8";
    public static final String SPACE_KEY = "space";
    public static final String SPACE_ID = "893646";
    public static final String USER_ID = "admin";
    public static final String USER_KEY = "f80808143087d180143087d3a910004";
    public static final String OUTPUT_TYPE = "display";

    @Parameterized.Parameters(name = "{0} is replaced")
    public static Collection<Object[]> testData()
    {
        return Arrays.asList(new Object[][]{
                {"macro.hash", HASH},
                {"macro.body", BODY},
                {"macro.truncated", "false"},
                {"page.type", PAGE_TYPE},
                {"page.id", PAGE_ID},
                {"page.title", PAGE_TITLE},
                {"page.version.id", PAGE_VERSION},
                {"space.key", SPACE_KEY},
                {"space.id", SPACE_ID},
                {"user.id", USER_ID},
                {"user.key", USER_KEY},
                {"output.type", OUTPUT_TYPE}
        });
    }

    protected UserManager userManager;
    protected ConversionContext conversionContext;

    protected RemotablePluginAccessorFactory remotablePluginAccessorFactory = new RemotablePluginAccessorFactoryForTests();
    protected UrlVariableSubstitutor urlVariableSubstitutor = new UrlVariableSubstitutor(new IsDevModeServiceImpl());

    private final String variable;
    private final String expectedValue;

    public AbstractContentMacroUrlTemplateTest(String variable, String expectedValue)
    {
        this.variable = variable;
        this.expectedValue = expectedValue;
    }

    protected abstract T createBeanBuilder();

    protected abstract M createMacro(B bean);

    protected abstract void verifyRendererInvokedWithQueryParameter(String name, String value) throws Exception;


    @Before
    public void beforeEachTest()
    {
        userManager = mock(UserManager.class);
        conversionContext = mock(ConversionContext.class);

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
    @Ignore
    public void variableIsReplaced() throws Exception
    {
        B bean = createBeanBuilder()
                .withUrl("/macro-renderer?param={" + variable + "}")
                .build();

        M macro = createMacro(bean);
        macro.execute(Maps.<String, String>newHashMap(), BODY, conversionContext);

        verifyRendererInvokedWithQueryParameter("param", expectedValue);
    }

}
