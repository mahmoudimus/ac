package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.DynamicContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;

@RunWith(MockitoJUnitRunner.class)
public class DynamicContentMacroModuleProviderTest extends AbstractContentMacroModuleProviderTest<DynamicContentMacroModuleProvider,
        DynamicContentMacroModuleBean, DynamicContentMacroModuleBeanBuilder>
{
    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private UserManager userManager;

    @Override
    protected DynamicContentMacroModuleProvider createModuleProvider()
    {
        DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new DynamicContentMacroModuleDescriptorFactory(
                remotablePluginAccessorFactoryForTests,
                iFrameRenderer,
                userManager,
                absoluteAddOnUrlConverter,
                i18nPropertiesPluginManager);

        return new DynamicContentMacroModuleProvider(macroModuleDescriptorFactory, webItemModuleDescriptorFactory,
                servletDescriptorFactory, hostContainer, absoluteAddOnUrlConverter, relativeAddOnUrlConverter);
    }

    @Override
    protected DynamicContentMacroModuleBeanBuilder createMacroBeanBuilder()
    {
        return newDynamicContentMacroModuleBean();
    }

}
