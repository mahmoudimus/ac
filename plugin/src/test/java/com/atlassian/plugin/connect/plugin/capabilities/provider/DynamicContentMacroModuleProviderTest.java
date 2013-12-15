package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.DynamicContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynamicContentMacroModuleProviderTest
{
    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private UserManager userManager;
    @Mock
    private UrlVariableSubstitutor urlVariableSubsitutor;
    @Mock
    private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private HostContainer hostContainer;
    @Mock
    private I18nPropertiesPluginManager i18nPropertiesPluginManager;

    private Plugin plugin = new PluginForTests("my-plugin", "My Plugin");
    private DynamicContentMacroModuleProvider moduleProvider;

    @Before
    public void beforeEachTest() throws Exception
    {
        when(webItemModuleDescriptorFactory.createModuleDescriptor(any(Plugin.class), any(BundleContext.class), any(WebItemModuleBean.class)))
                .thenReturn(mock(WebItemModuleDescriptor.class));

        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();

        DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new DynamicContentMacroModuleDescriptorFactory(
                remotablePluginAccessorFactoryForTests,
                iFrameRenderer,
                userManager,
                hostContainer,
                urlVariableSubsitutor,
                new AbsoluteAddOnUrlConverter(remotablePluginAccessorFactoryForTests),
                i18nPropertiesPluginManager);

        moduleProvider = new DynamicContentMacroModuleProvider(macroModuleDescriptorFactory, webItemModuleDescriptorFactory);
    }

    @Test
    public void testSimpleMacro() throws Exception
    {
        DynamicContentMacroModuleBean bean = newDynamicContentMacroModuleBean()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .build();
        List<ModuleDescriptor> modules = moduleProvider.provideModules(plugin, bundleContext, "", Lists.newArrayList(bean));
        assertThat(modules, containsInAnyOrder(is(instanceOf(XhtmlMacroModuleDescriptor.class))));
    }

    @Test
    public void testFeaturedMacro() throws Exception
    {
        DynamicContentMacroModuleBean bean = newDynamicContentMacroModuleBean()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withFeatured(true)
                .build();
        List<ModuleDescriptor> modules = moduleProvider.provideModules(plugin, bundleContext, "", Lists.newArrayList(bean));
        assertThat(modules, containsInAnyOrder(
                is(instanceOf(XhtmlMacroModuleDescriptor.class)),
                is(instanceOf(WebItemModuleDescriptor.class))
        ));
    }

    //@Test Weird issue with java.lang.NoSuchMethodError in WebResourceModuleDescriptor
    public void testFeaturedMacroWithIcon() throws Exception
    {
        DynamicContentMacroModuleBean bean = newDynamicContentMacroModuleBean()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withFeatured(true)
                .withIcon(IconBean.newIconBean().withUrl("/assets/image.png").build())
                .build();
        List<ModuleDescriptor> modules = moduleProvider.provideModules(plugin, bundleContext, "", Lists.newArrayList(bean));
        assertThat(modules, containsInAnyOrder(
                is(instanceOf(XhtmlMacroModuleDescriptor.class)),
                is(instanceOf(WebItemModuleDescriptor.class)),
                is(instanceOf(WebResourceModuleDescriptor.class))
        ));
    }
}
