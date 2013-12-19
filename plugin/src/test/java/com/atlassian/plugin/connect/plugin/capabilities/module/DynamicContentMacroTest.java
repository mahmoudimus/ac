package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class DynamicContentMacroTest extends AbstractContentMacroTest<DynamicContentMacroModuleBean, DynamicContentMacro, DynamicContentMacroModuleBeanBuilder>
{
    @Mock
    private IFrameRenderer iFrameRenderer;

    @Override
    protected DynamicContentMacroModuleBeanBuilder createBeanBuilder()
    {
        return newDynamicContentMacroModuleBean();
    }

    protected DynamicContentMacro createMacro(DynamicContentMacroModuleBean bean)
    {
        return new DynamicContentMacro("my-plugin", bean, userManager, iFrameRenderer, remotablePluginAccessorFactory, urlVariableSubstitutor);
    }

    protected void verifyRendererInvokedWithQueryParameter(String name, String value) throws Exception
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
}
