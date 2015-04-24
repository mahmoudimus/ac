package com.atlassian.plugin.connect.test.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.MacroEnumMapper;
import com.atlassian.plugin.connect.confluence.macro.DynamicContentMacro;
import com.atlassian.plugin.connect.confluence.macro.RemoteMacroRenderer;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.ContentEntityForTests;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentMacroTest
{
    @Mock private UserManager userManager;
    @Mock private ConversionContext conversionContext;
    @Mock private RemoteMacroRenderer remoteMacroRenderer;

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

//      TODO  verify(iFrameRenderer).render(argThat(hasIFrameParam("width", "300px")), anyString(), anyMap(), anyString(), anyMap());
    }

    @Test
    public void iFrameHeightIsSet() throws Exception
    {
        executeMacro("300px", "100px");

//      TODO  verify(iFrameRenderer).render(argThat(hasIFrameParam("height", "100px")), anyString(), anyMap(), anyString(), anyMap());
    }

    private void executeMacro(String width, String height) throws MacroExecutionException
    {
        DynamicContentMacroModuleBean bean = newDynamicContentMacroModuleBean()
                .withUrl("/macro-renderer")
                .withWidth(width)
                .withHeight(height)
                .build();

        DynamicContentMacro macro = new DynamicContentMacro(
//                addOnKey, moduleKey,
                "addon-key", "module-key",
                MacroEnumMapper.map(bean.getBodyType()),
                MacroEnumMapper.map(bean.getOutputType()), remoteMacroRenderer,
                MacroRenderModesBean.newMacroRenderModesBean().build());
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
