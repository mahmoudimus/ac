package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro.StaticContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.module.macro.MacroModuleContextExtractor;
import com.atlassian.plugin.connect.plugin.capabilities.module.macro.RemoteMacroRenderer;
import com.atlassian.plugin.connect.plugin.capabilities.provider.StaticContentMacroModuleProvider;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;

@ConvertToWiredTest
@Ignore("replace with wired tests")
@RunWith(MockitoJUnitRunner.class)
public class StaticContentMacroModuleProviderTest extends AbstractContentMacroModuleProviderTest<StaticContentMacroModuleProvider,
        StaticContentMacroModuleBean, StaticContentMacroModuleBeanBuilder>
{
    @Mock private RemoteMacroRenderer remoteMacroRenderer;

    @Override
    protected StaticContentMacroModuleProvider createModuleProvider()
    {
        StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new StaticContentMacroModuleDescriptorFactory(
                absoluteAddOnUrlConverter, remoteMacroRenderer);

        return new StaticContentMacroModuleProvider(macroModuleDescriptorFactory, webItemModuleDescriptorFactory,
                hostContainer, absoluteAddOnUrlConverter, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory,
                connectAddonI18nManager);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder createMacroBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }

}
