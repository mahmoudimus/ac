package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import org.mockito.ArgumentMatcher;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class DynamicContentMacroUrlTemplateTest extends AbstractContentMacroUrlTemplateTest<DynamicContentMacroModuleBean, DynamicContentMacro, DynamicContentMacroModuleBeanBuilder>
{
    private IFrameRenderer iFrameRenderer;

    public DynamicContentMacroUrlTemplateTest(String variable, String expectedValue)
    {
        super(variable, expectedValue);
        iFrameRenderer = mock(IFrameRenderer.class);
    }

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
