package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.EditorImagePlaceholder;
import com.atlassian.confluence.macro.ImagePlaceholder;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.provider.AbstractContentMacroModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilder;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.integration.plugins.ConnectAddonI18nManager;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean.newMacroEditorBean;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractContentMacroModuleProviderTest<P extends AbstractContentMacroModuleProvider,
        B extends BaseContentMacroModuleBean, T extends BaseContentMacroModuleBeanBuilder<T, B>>
{
    @Mock
    protected WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock
    protected BundleContext bundleContext;
    @Mock
    protected HostContainer hostContainer;
    @Mock
    protected ConnectAddonI18nManager connectAddonI18nManager;

    @Mock
    protected IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    @Mock
    protected IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    @Mock
    protected IFrameRenderStrategyBuilder iFrameRenderStrategyBuilder;
    @Mock
    

    protected Plugin plugin = new PluginForTests("my-plugin", "My Plugin");
    protected RemotablePluginAccessorFactory remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();
    protected AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter = new AbsoluteAddOnUrlConverter(remotablePluginAccessorFactoryForTests);

    protected ConnectAddonBean addon = newConnectAddonBean().withKey("my-plugin").build();
    private ConnectModuleProviderContext moduleProviderContext = new DefaultConnectModuleProviderContext(addon);


    private P moduleProvider;


    protected abstract P createModuleProvider();

    protected abstract T createMacroBeanBuilder();

    @Before
    public void beforeEachTest() throws Exception
    {
        when(webItemModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, any(Plugin.class), any(WebItemModuleBean.class)))
                .thenReturn(mock(WebItemModuleDescriptor.class));
        
        when(iFrameRenderStrategyBuilderFactory.builder()).thenReturn(iFrameRenderStrategyBuilder);

        moduleProvider = createModuleProvider();
    }

    @Test
    public void testSimpleMacro() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro");
        List<ModuleDescriptor> modules = moduleProvider.provideModules(moduleProviderContext, plugin, "", Lists.newArrayList(builder.build()));
        assertThat(modules, containsInAnyOrder(is(instanceOf(XhtmlMacroModuleDescriptor.class))));
    }

    @Test
    public void testFeaturedMacro() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withFeatured(true);
        List<ModuleDescriptor> modules = moduleProvider.provideModules(moduleProviderContext, plugin, "", Lists.newArrayList(builder.build()));
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
        List<ModuleDescriptor> modules = moduleProvider.provideModules(moduleProviderContext, plugin, "", Lists.newArrayList(builder.build()));
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
        List<ModuleDescriptor> modules = moduleProvider.provideModules(moduleProviderContext, plugin, "", Lists.newArrayList(builder.build()));
        assertThat(modules, containsInAnyOrder(
                is(instanceOf(XhtmlMacroModuleDescriptor.class)),
                is(instanceOf(ServletModuleDescriptor.class)),
                is(instanceOf(WebResourceModuleDescriptor.class))
        ));
    }

    @Test
    public void testMacroWithImagePlaceholder() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                                          .withUrl("/images/placeholder.png")
                                                          .build()
                );
        List<ModuleDescriptor> modules = moduleProvider.provideModules(moduleProviderContext, plugin, "", Lists.newArrayList(builder.build()));
        Macro macro = getMacro(modules);
        assertThat(macro, is(instanceOf(EditorImagePlaceholder.class)));
    }

    @Test
    public void testImagePlaceholderUrl() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                                          .withUrl("/images/placeholder.png")
                                                          .build()
                );
        ImagePlaceholder imagePlaceHolder = getImagePlaceholder(builder, ImmutableMap.<String, String>of());
        assertThat(imagePlaceHolder.getUrl(), is("http://www.example.com/images/placeholder.png"));
    }

    @Test
    public void testImagePlaceholderUrlWithParameters() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                                          .withUrl("/images/placeholder.png")
                                                          .build()
                );
        ImagePlaceholder imagePlaceHolder = getImagePlaceholder(builder, ImmutableMap.<String, String>of(
                "p1", "v1",
                "p2", "v2")
        );
        assertThat(imagePlaceHolder.getUrl(), is("http://www.example.com/images/placeholder.png?p1=v1&p2=v2"));
    }

    @Test
    public void testImagePlaceholderDimensions() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                                          .withUrl("/images/placeholder.png")
                                                          .withWidth(60)
                                                          .withHeight(30)
                                                          .build()
                );
        ImagePlaceholder imagePlaceHolder = getImagePlaceholder(builder, ImmutableMap.<String, String>of());
        assertThat(imagePlaceHolder.getDimensions(),
                both(hasProperty("width", is(60))).and(hasProperty("height", is(30))));
    }

    @Test
    public void testImagePlaceholderNoDimensions() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                                          .withUrl("/images/placeholder.png")
                                                          .build()
                );
        ImagePlaceholder imagePlaceHolder = getImagePlaceholder(builder, ImmutableMap.<String, String>of());
        assertThat(imagePlaceHolder.getDimensions(), is(nullValue()));
    }

    @Test
    public void testImagePlaceholderApplyChromeTrue() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                                          .withUrl("/images/placeholder.png")
                                                          .withApplyChrome(true)
                                                          .build()
                );
        ImagePlaceholder imagePlaceHolder = getImagePlaceholder(builder, ImmutableMap.<String, String>of());
        assertThat(imagePlaceHolder.applyPlaceholderChrome(), is(true));
    }

    @Test
    public void testImagePlaceholderApplyChromeFalse() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                                          .withUrl("/images/placeholder.png")
                                                          .withApplyChrome(false)
                                                          .build()
                );
        ImagePlaceholder imagePlaceHolder = getImagePlaceholder(builder, ImmutableMap.<String, String>of());
        assertThat(imagePlaceHolder.applyPlaceholderChrome(), is(false));
    }

    @Test
    public void testImagePlaceholderApplyChromeDefault() throws Exception
    {
        T builder = createMacroBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                                          .withUrl("/images/placeholder.png")
                                                          .build()
                );
        ImagePlaceholder imagePlaceHolder = getImagePlaceholder(builder, ImmutableMap.<String, String>of());
        assertThat(imagePlaceHolder.applyPlaceholderChrome(), is(false));
    }

    private ImagePlaceholder getImagePlaceholder(T builder, Map<String, String> parameters)
    {
        List<ModuleDescriptor> modules = moduleProvider.provideModules(moduleProviderContext, plugin, "", Lists.newArrayList(builder.build()));
        EditorImagePlaceholder macro = (EditorImagePlaceholder) getMacro(modules);
        return macro.getImagePlaceholder(parameters, mock(ConversionContext.class));
    }

    private Macro getMacro(List<ModuleDescriptor> modules)
    {
        XhtmlMacroModuleDescriptor descriptor = (XhtmlMacroModuleDescriptor) modules.get(0);
        descriptor.enabled();
        return descriptor.getModule();
    }
}
