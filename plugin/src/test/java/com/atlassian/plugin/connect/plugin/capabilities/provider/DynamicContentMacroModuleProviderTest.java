package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.DynamicContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
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
    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;

    @Override
    protected DynamicContentMacroModuleProvider createModuleProvider()
    {
        DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new DynamicContentMacroModuleDescriptorFactory(
                absoluteAddOnUrlConverter,
                i18nPropertiesPluginManager,
                iFrameRenderer,
                userManager,
                remotablePluginAccessorFactoryForTests,
                urlVariableSubstitutor);

        return new DynamicContentMacroModuleProvider(macroModuleDescriptorFactory, webItemModuleDescriptorFactory,
                servletDescriptorFactory, hostContainer, absoluteAddOnUrlConverter, relativeAddOnUrlConverter);
    }

    @Override
    protected DynamicContentMacroModuleBeanBuilder createMacroBeanBuilder()
    {
        return newDynamicContentMacroModuleBean();
    }

}
