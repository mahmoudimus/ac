package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
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

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentMacroTest
{
    @Mock
    private UserManager userManager;
    @Mock
    private ConversionContext conversionContext;
    @Mock
    private IFrameRenderer iFrameRenderer;

    private RemotablePluginAccessorFactory remotablePluginAccessorFactory = new RemotablePluginAccessorFactoryForTests();
    private UrlVariableSubstitutor urlVariableSubstitutor = new UrlVariableSubstitutor();


    @Before
    public void beforeEachTest()
    {
        ContentEntityObject contentEntity = new ContentEntityForTests();
        UserProfile user = mock(UserProfile.class);
        UserKey userKey = new UserKey("xyz");

        when(conversionContext.getEntity()).thenReturn(contentEntity);
        when(conversionContext.getOutputType()).thenReturn("display");
        when(userManager.getRemoteUser()).thenReturn(user);

        when(user.getUsername()).thenReturn("admin");
        when(user.getUserKey()).thenReturn(userKey);
    }

    @Test
    public void iFrameWidthIsSet() throws Exception
    {
        executeMacro("300px", "100px");

        verify(iFrameRenderer).render(argThat(hasIFrameParam("width", "300px")), anyString(), anyMap(), anyString(), anyMap());
    }

    @Test
    public void iFrameHeightIsSet() throws Exception
    {
        executeMacro("300px", "100px");

        verify(iFrameRenderer).render(argThat(hasIFrameParam("height", "100px")), anyString(), anyMap(), anyString(), anyMap());
    }

    private void executeMacro(String width, String height) throws MacroExecutionException
    {
        DynamicContentMacroModuleBean bean = newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer")
                .withWidth(width)
                .withHeight(height)
                .build();

        DynamicContentMacro macro = new DynamicContentMacro("my-plugin", bean, userManager, iFrameRenderer, remotablePluginAccessorFactory, urlVariableSubstitutor);
        macro.execute(Maps.<String, String>newHashMap(), "some macro content", conversionContext);
    }

    private ArgumentMatcher<IFrameContext> hasIFrameParam(final String name, final String value)
    {
        return new ArgumentMatcher<IFrameContext>()
        {
            @Override
            public boolean matches(Object actual)
            {
                IFrameContext iFrameContext = (IFrameContext) actual;
                Map<String, Object> params = iFrameContext.getIFrameParams().getAsMap();
                return value.equals(params.get(name));
            }
        };
    }
}
