package com.atlassian.plugin.connect.confluence.capabilities.descriptor.macro;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.confluence.macro.MacroContentManager;
import com.atlassian.plugin.connect.confluence.macro.MacroModuleContextExtractor;
import com.atlassian.plugin.connect.confluence.macro.RemoteMacroRendererImpl;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.util.fixture.RemotablePluginAccessorFactoryForTests;
import com.atlassian.sal.api.user.UserManager;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class StaticContentMacroModuleDescriptorTest extends AbstractContentMacroModuleDescriptorTest<StaticContentMacroModuleBean, StaticContentMacroModuleBeanBuilder>
{
    @Mock private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    @Mock private IFrameUriBuilderFactory iFrameUriBuilderFactory;
    @Mock private MacroModuleContextExtractor macroModuleContextExtractor;
    @Mock private RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    @Mock private MacroContentManager macroContentManager;
    @Mock private UserManager userManager;
    @Mock private UrlVariableSubstitutor urlVariableSubstitutor;

    @Override
    protected XhtmlMacroModuleDescriptor createModuleDescriptorForTest()
    {
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();

        StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new StaticContentMacroModuleDescriptorFactory(
                absoluteAddOnUrlConverter,
                new RemoteMacroRendererImpl(iFrameUriBuilderFactory, macroModuleContextExtractor, macroContentManager, remotablePluginAccessorFactoryForTests, iFrameRenderStrategyRegistry));

        StaticContentMacroModuleBean bean = createBeanBuilder()
                .build();

        final ConnectModuleProviderContext moduleProviderContext = mock(ConnectModuleProviderContext.class);
        when(moduleProviderContext.getConnectAddonBean()).thenReturn(addon);
        return macroModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder newContentMacroModuleBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }
}
