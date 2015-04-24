package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.mockito.ArgumentMatcher;

import java.net.URI;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StaticContentMacroUrlTemplateTest extends AbstractContentMacroUrlTemplateTest<StaticContentMacroModuleBean, StaticContentMacro, StaticContentMacroModuleBeanBuilder>
{
    private MacroContentManager macroContentManager;

    public StaticContentMacroUrlTemplateTest(String variable, String expectedValue)
    {
        super(variable, expectedValue);
        macroContentManager = mock(MacroContentManager.class);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder createBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }

    protected StaticContentMacro createMacro(StaticContentMacroModuleBean bean)
    {
        return mock(StaticContentMacro.class); // TODO
    }

    protected void verifyRendererInvokedWithQueryParameter(String name, String value) throws Exception
    {
        verify(macroContentManager).getStaticContent(any(HttpMethod.class), any(URI.class), argThat(hasQueryParam(name, value)),
                any(ConversionContext.class), any(RemotablePluginAccessor.class));
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
