package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DynamicContentMacroModuleDescriptorTest extends AbstractContentMacroModuleDescriptorTest<DynamicContentMacroModuleBean, DynamicContentMacroModuleBeanBuilder>
{
    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private UserManager userManager;
    @Mock
    private I18nPropertiesPluginManager i18nPropertiesPluginManager;

    @Override
    protected XhtmlMacroModuleDescriptor createModuleDescriptorForTest()
    {
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();

        DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new DynamicContentMacroModuleDescriptorFactory(
                remotablePluginAccessorFactoryForTests,
                iFrameRenderer,
                userManager,
                new AbsoluteAddOnUrlConverter(remotablePluginAccessorFactoryForTests),
                i18nPropertiesPluginManager);

        DynamicContentMacroModuleBean bean = createBeanBuilder().build();

        return macroModuleDescriptorFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
    }

    @Override
    protected DynamicContentMacroModuleBeanBuilder newContentMacroModuleBeanBuilder()
    {
        return newDynamicContentMacroModuleBean();
    }
}
