package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.StaticContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.sal.api.user.UserManager;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;

@RunWith(MockitoJUnitRunner.class)
public class StaticContentMacroModuleProviderTest extends AbstractContentMacroModuleProviderTest<StaticContentMacroModuleProvider,
        StaticContentMacroModuleBean, StaticContentMacroModuleBeanBuilder>
{
    @Mock
    private MacroContentManager macroContentManager;
    @Mock
    private UserManager userManager;
    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;

    @Override
    protected StaticContentMacroModuleProvider createModuleProvider()
    {
        StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new StaticContentMacroModuleDescriptorFactory(
                absoluteAddOnUrlConverter,
                i18nPropertiesPluginManager,
                macroContentManager,
                userManager,
                remotablePluginAccessorFactoryForTests,
                urlVariableSubstitutor);

        return new StaticContentMacroModuleProvider(macroModuleDescriptorFactory, webItemModuleDescriptorFactory,
                servletDescriptorFactory, hostContainer, absoluteAddOnUrlConverter, relativeAddOnUrlConverter);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder createMacroBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }

}
