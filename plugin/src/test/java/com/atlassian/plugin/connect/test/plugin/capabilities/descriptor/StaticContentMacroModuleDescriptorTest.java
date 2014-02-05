package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro.StaticContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.MacroModuleContextExtractor;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.sal.api.user.UserManager;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;

@RunWith(MockitoJUnitRunner.class)
public class StaticContentMacroModuleDescriptorTest extends AbstractContentMacroModuleDescriptorTest<StaticContentMacroModuleBean, StaticContentMacroModuleBeanBuilder>
{
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
                macroContentManager,
                macroModuleContextExtractor,
                iFrameUriBuilderFactory,
                remotablePluginAccessorFactoryForTests);

        StaticContentMacroModuleBean bean = createBeanBuilder()
                .build();

        return macroModuleDescriptorFactory.createModuleDescriptor(plugin, bean);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder newContentMacroModuleBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }
}
