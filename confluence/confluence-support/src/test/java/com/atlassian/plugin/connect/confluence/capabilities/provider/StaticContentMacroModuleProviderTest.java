package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.macro.StaticContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.confluence.macro.RemoteMacroRenderer;
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

        return new StaticContentMacroModuleProvider(pluginRetrievalService, schemaValidator, macroModuleDescriptorFactory,
                webItemModuleDescriptorFactory, hostContainer, absoluteAddOnUrlConverter, iFrameRenderStrategyRegistry,
                iFrameRenderStrategyBuilderFactory, connectAddonI18nManager);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder createMacroBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }

}
