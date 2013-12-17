package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.MacroEditorBean.newMacroEditorBean;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractContentMacroModuleProviderTest<P extends AbstractContentMacroModuleProvider,
        B extends BaseContentMacroModuleBean, T extends BaseContentMacroModuleBeanBuilder<T, B>>
{
    @Mock
    protected WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock
    protected IFramePageServletDescriptorFactory servletDescriptorFactory;
    @Mock
    protected BundleContext bundleContext;
    @Mock
    protected HostContainer hostContainer;
    @Mock
    protected I18nPropertiesPluginManager i18nPropertiesPluginManager;

    protected Plugin plugin = new PluginForTests("my-plugin", "My Plugin");
    protected RemotablePluginAccessorFactory remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();
    protected AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter = new AbsoluteAddOnUrlConverter(remotablePluginAccessorFactoryForTests);
    protected RelativeAddOnUrlConverter relativeAddOnUrlConverter = new RelativeAddOnUrlConverter();

    private P moduleProvider;


    protected abstract P createModuleProvider();
    protected abstract T createMacroBeanBuilder();

    @Before
    public void beforeEachTest() throws Exception
    {
        when(webItemModuleDescriptorFactory.createModuleDescriptor(any(Plugin.class), any(BundleContext.class), any(WebItemModuleBean.class)))
                .thenReturn(mock(WebItemModuleDescriptor.class));
        when(servletDescriptorFactory.createIFrameServletDescriptor(any(Plugin.class), any(IFrameServletBean.class)))
                .thenReturn(mock(ServletModuleDescriptor.class));

        moduleProvider = createModuleProvider();
    }

    @Test
    public void testSimpleMacro() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro");
        List<ModuleDescriptor> modules = moduleProvider.provideModules(plugin, bundleContext, "", Lists.newArrayList(builder.build()));
        assertThat(modules, containsInAnyOrder(is(instanceOf(XhtmlMacroModuleDescriptor.class))));
    }

    @Test
    public void testFeaturedMacro() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withFeatured(true);
        List<ModuleDescriptor> modules = moduleProvider.provideModules(plugin, bundleContext, "", Lists.newArrayList(builder.build()));
        assertThat(modules, containsInAnyOrder(
                is(instanceOf(XhtmlMacroModuleDescriptor.class)),
                is(instanceOf(WebItemModuleDescriptor.class))
        ));
    }

    @Test
    public void testFeaturedMacroWithIcon() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withFeatured(true)
                .withIcon(IconBean.newIconBean().withUrl("/assets/image.png").build());
        List<ModuleDescriptor> modules = moduleProvider.provideModules(plugin, bundleContext, "", Lists.newArrayList(builder.build()));
        assertThat(modules, containsInAnyOrder(
                is(instanceOf(XhtmlMacroModuleDescriptor.class)),
                is(instanceOf(WebItemModuleDescriptor.class)),
                is(instanceOf(WebResourceModuleDescriptor.class))
        ));
    }

    @Test
    public void testMacroWithEditor() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withEditor(newMacroEditorBean()
                        .withUrl("/macro-editor")
                        .withEditTitle(new I18nProperty("Edit Title", ""))
                        .withInsertTitle(new I18nProperty("Insert Title", ""))
                        .withWidth("100px")
                        .withHeight("100px")
                        .build()
                );
        List<ModuleDescriptor> modules = moduleProvider.provideModules(plugin, bundleContext, "", Lists.newArrayList(builder.build()));
        assertThat(modules, containsInAnyOrder(
                is(instanceOf(XhtmlMacroModuleDescriptor.class)),
                is(instanceOf(ServletModuleDescriptor.class)),
                is(instanceOf(WebResourceModuleDescriptor.class))
        ));
    }
}
