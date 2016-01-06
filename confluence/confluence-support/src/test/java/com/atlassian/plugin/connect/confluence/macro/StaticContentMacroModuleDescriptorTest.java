package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.web.iframe.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.util.fixture.RemotablePluginAccessorFactoryForTests;
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
                absoluteAddonUrlConverter,
                new RemoteMacroRendererImpl(iFrameUriBuilderFactory, macroModuleContextExtractor, macroContentManager, remotablePluginAccessorFactoryForTests, iFrameRenderStrategyRegistry));

        StaticContentMacroModuleBean bean = createBeanBuilder()
                .build();

        return macroModuleDescriptorFactory.createModuleDescriptor(bean, addon, plugin);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder newContentMacroModuleBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }
}
