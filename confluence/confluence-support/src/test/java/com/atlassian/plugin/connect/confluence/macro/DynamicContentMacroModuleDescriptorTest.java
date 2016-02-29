package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.test.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.test.fixture.RemotablePluginAccessorFactoryForTests;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class DynamicContentMacroModuleDescriptorTest extends AbstractContentMacroModuleDescriptorTest<DynamicContentMacroModuleBean, DynamicContentMacroModuleBeanBuilder> {
    @Mock
    private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    @Mock
    private MacroModuleContextExtractor macroModuleContextExtractor;
    @Mock
    private ConnectUriFactory connectUriFactory;
    @Mock
    private MacroContentManager macroContentManager;

    @Override
    protected XhtmlMacroModuleDescriptor createModuleDescriptorForTest() {
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();

        DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new DynamicContentMacroModuleDescriptorFactory(
                absoluteAddonUrlConverter,
                new RemoteMacroRendererImpl(connectUriFactory, macroModuleContextExtractor, macroContentManager, remotablePluginAccessorFactoryForTests, iFrameRenderStrategyRegistry));

        DynamicContentMacroModuleBean bean = createBeanBuilder().build();

        return macroModuleDescriptorFactory.createModuleDescriptor(bean, addon, plugin);
    }

    @Override
    protected DynamicContentMacroModuleBeanBuilder newContentMacroModuleBeanBuilder() {
        return newDynamicContentMacroModuleBean();
    }
}
