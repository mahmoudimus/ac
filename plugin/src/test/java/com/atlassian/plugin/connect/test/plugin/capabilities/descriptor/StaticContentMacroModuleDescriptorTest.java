package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro.StaticContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.macro.MacroModuleContextExtractor;
import com.atlassian.plugin.connect.plugin.capabilities.module.macro.RemoteMacroRendererImpl;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.sal.api.user.UserManager;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;

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
                new AbsoluteAddOnUrlConverter(remotablePluginAccessorFactoryForTests),
                new RemoteMacroRendererImpl(iFrameUriBuilderFactory, macroModuleContextExtractor, macroContentManager, remotablePluginAccessorFactoryForTests, iFrameRenderStrategyRegistry));

        StaticContentMacroModuleBean bean = createBeanBuilder()
                .build();

        return macroModuleDescriptorFactory.createModuleDescriptor(new DefaultConnectModuleProviderContext(addon), plugin, bean);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder newContentMacroModuleBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }
}
