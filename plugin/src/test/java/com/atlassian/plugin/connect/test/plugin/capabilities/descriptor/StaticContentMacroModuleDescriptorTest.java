package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.MacroHttpMethod;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.StaticContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.sal.api.user.UserManager;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class StaticContentMacroModuleDescriptorTest extends AbstractContentMacroModuleDescriptorTest<StaticContentMacroModuleBean, StaticContentMacroModuleBeanBuilder>
{
    @Mock
    private MacroContentManager macroContentManager;
    @Mock
    private UserManager userManager;
    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;

    @Override
    protected XhtmlMacroModuleDescriptor createModuleDescriptorForTest()
    {
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();

        StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new StaticContentMacroModuleDescriptorFactory(
                new AbsoluteAddOnUrlConverter(remotablePluginAccessorFactoryForTests),
                macroContentManager,
                userManager,
                remotablePluginAccessorFactoryForTests,
                urlVariableSubstitutor);

        StaticContentMacroModuleBean bean = createBeanBuilder()
                .withMethod(MacroHttpMethod.POST)
                .build();

        return macroModuleDescriptorFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
    }

    @Override
    protected StaticContentMacroModuleBeanBuilder newContentMacroModuleBeanBuilder()
    {
        return newStaticContentMacroModuleBean();
    }
}
