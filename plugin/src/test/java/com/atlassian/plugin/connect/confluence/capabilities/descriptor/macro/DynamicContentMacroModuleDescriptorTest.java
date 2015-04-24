package com.atlassian.plugin.connect.confluence.capabilities.descriptor.macro;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.confluence.macro.MacroContentManager;
import com.atlassian.plugin.connect.confluence.macro.MacroModuleContextExtractor;
import com.atlassian.plugin.connect.confluence.macro.RemoteMacroRendererImpl;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.util.fixture.RemotablePluginAccessorFactoryForTests;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class DynamicContentMacroModuleDescriptorTest extends AbstractContentMacroModuleDescriptorTest<DynamicContentMacroModuleBean, DynamicContentMacroModuleBeanBuilder>
{
    @Mock private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    @Mock private MacroModuleContextExtractor macroModuleContextExtractor;
    @Mock private IFrameUriBuilderFactory iFrameUriBuilderFactory;
    @Mock private MacroContentManager macroContentManager;

    @Override
    protected XhtmlMacroModuleDescriptor createModuleDescriptorForTest()
    {
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();

        DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new DynamicContentMacroModuleDescriptorFactory(
                new AbsoluteAddOnUrlConverter(remotablePluginAccessorFactoryForTests),
                new RemoteMacroRendererImpl(iFrameUriBuilderFactory, macroModuleContextExtractor, macroContentManager, remotablePluginAccessorFactoryForTests, iFrameRenderStrategyRegistry));

        DynamicContentMacroModuleBean bean = createBeanBuilder().build();
        return macroModuleDescriptorFactory.createModuleDescriptor(new DefaultConnectModuleProviderContext(addon), plugin, bean);
    }

    @Override
    protected DynamicContentMacroModuleBeanBuilder newContentMacroModuleBeanBuilder()
    {
        return newDynamicContentMacroModuleBean();
    }
}
