package com.atlassian.plugin.connect.test.plugin.capabilities.module;

import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.module.DynamicContentMacro;
import com.atlassian.plugin.connect.plugin.capabilities.module.MacroModuleContextExtractor;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;


public class DynamicContentMacroUrlTemplateTest extends AbstractContentMacroUrlTemplateTest<DynamicContentMacroModuleBean, DynamicContentMacro, DynamicContentMacroModuleBeanBuilder>
{
    @Mock private IFrameRenderStrategy iFrameRenderStrategy;
    @Mock private MacroModuleContextExtractor macroModuleContextExtractor;

    public DynamicContentMacroUrlTemplateTest(String variable, String expectedValue)
    {
        super(variable, expectedValue);
    }

    @Override
    protected DynamicContentMacroModuleBeanBuilder createBeanBuilder()
    {
        return newDynamicContentMacroModuleBean();
    }

    protected DynamicContentMacro createMacro(DynamicContentMacroModuleBean bean)
    {
        return new DynamicContentMacro(MacroEnumMapper.map(bean.getBodyType()), MacroEnumMapper.map(bean.getOutputType()), iFrameRenderStrategy, macroModuleContextExtractor);
    }

    protected void verifyRendererInvokedWithQueryParameter(String name, String value) throws Exception
    {
        // TODO
//        verify(iFrameRenderer).render(any(IFrameContext.class), anyString(), argThat(hasQueryParam(name, value)), anyString(), anyMap());
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
