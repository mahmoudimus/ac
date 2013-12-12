package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.macro.browser.beans.MacroIcon;
import com.atlassian.confluence.macro.browser.beans.MacroParameter;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroCategory;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterType;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.Mockito.mock;


@RunWith(MockitoJUnitRunner.class)
public class DynamicContentMacroModuleDescriptorTest
{
    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private UserManager userManager;
    @Mock
    private UrlVariableSubstitutor urlVariableSubsitutor;

    private Plugin plugin = new PluginForTests("my-plugin", "My Plugin");
    private XhtmlMacroModuleDescriptor descriptor;

    @Before
    public void beforeEachTest() throws Exception
    {
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();

        DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory = new DynamicContentMacroModuleDescriptorFactory(
                remotablePluginAccessorFactoryForTests,
                iFrameRenderer,
                userManager,
                urlVariableSubsitutor);

        DynamicContentMacroModuleBean bean = createBean();

        this.descriptor = macroModuleDescriptorFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        this.descriptor.enabled();
    }

    @Test
    public void verifyPluginKeyIsCorrect() throws Exception
    {
        assertThat(descriptor.getPluginKey(), is("my-plugin"));
    }

    @Test
    public void verifyKeyIsCorrect()
    {
        assertThat(descriptor.getKey(), is("the-macro-name"));
    }

    @Test
    public void verifyCompleteKeyIsCorrect()
    {
        assertThat(descriptor.getCompleteKey(), is("my-plugin:the-macro-name"));
    }

    @Test
    public void verifyNameIsSet() throws Exception
    {
        assertThat(descriptor.getName(), is("The Macro Name"));
    }

    @Test
    public void verifyMacroNameIsSet() throws Exception
    {
        assertThat(descriptor.getMacroMetadata().getMacroName(), is("The Macro Name"));
    }

    @Test
    public void verifyMacroTitleIsSet() throws Exception
    {
        assertThat(descriptor.getMacroMetadata().getTitle().getKey(), is("macro.name.key"));
    }

    @Test
    public void verifyMacroDescriptionIsSet() throws Exception
    {
        assertThat(descriptor.getMacroMetadata().getDescription().getKey(), is("macro.desc.key"));
    }

    @Test
    public void verifyModuleIsCreated() throws Exception
    {
        assertThat(descriptor.getModule(), is(not(nullValue())));
    }

    @Test
    public void verifyAliasNames() throws Exception
    {
        String[] aliases = descriptor.getMacroMetadata().getAliases().toArray(new String[2]);
        assertThat(aliases, arrayContainingInAnyOrder("alias1", "alias2"));
    }

    @Test
    public void verifyCategoryNames() throws Exception
    {
        String[] categories = descriptor.getMacroMetadata().getCategories().toArray(new String[2]);
        assertThat(categories, arrayContainingInAnyOrder(
                MacroCategory.MEDIA.toString(),
                MacroCategory.CONFLUENCE_CONTENT.toString()));
    }

    @Test
    public void verifyIconAttributes() throws Exception
    {
        MacroIcon icon = descriptor.getMacroMetadata().getIcon();
        assertThat(icon, allOf(
                hasProperty("width", is(80)),
                hasProperty("height", is(80)),
                hasProperty("location", is("/assets/macro.png"))
        ));
    }

    @Test
    public void verifyParameters() throws Exception
    {
        List<MacroParameter> parameters = descriptor.getMacroMetadata().getFormDetails().getParameters();
        MacroParameter macroParameter = parameters.get(0);
        assertThat(macroParameter, allOf(
                hasProperty("defaultValue", is("default")),
                hasProperty("name", is("Parameter Name")),
                hasProperty("type", hasToString(MacroParameterType.STRING.toString())),
                hasProperty("multiple", is(false)),
                hasProperty("required", is(true))
        ));
    }


    @Test
    public void verifyDocumentationLink() throws Exception
    {
        String documentationUrl = descriptor.getMacroMetadata().getFormDetails().getDocumentationUrl();
        assertThat(documentationUrl, is("http://docs.example.com/macro"));
    }

    @Test
    public void verifyIsNoSystemModule() throws Exception
    {
        assertThat(descriptor.isSystemModule(), is(false));
    }

    @Test
    public void verifyIsEnabledByDefault() throws Exception
    {
        assertThat(descriptor.isEnabledByDefault(), is(true));
    }

    private DynamicContentMacroModuleBean createBean()
    {
        return newDynamicContentMacroModuleBean()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withUrl("/my-macro")
                .withAliases("alias1", "alias2")
                .withBodyType(MacroBodyType.PLAIN_TEXT)
                .withOutputType(MacroOutputType.BLOCK)
                .withCategories(MacroCategory.MEDIA, MacroCategory.CONFLUENCE_CONTENT)
                .withDescription(new I18nProperty("The Macro Description", "macro.desc.key"))
                .withDocumentationUrl("http://docs.example.com/macro")
                .withIcon(newIconBean().withUrl("/assets/macro.png").withHeight(80).withWidth(80).build())
                .withParameters(newMacroParameterBean()
                        .withName("Parameter Name")
                        .withType(MacroParameterType.STRING)
                        .withDefaultValue("default")
                        .withMultiple(false)
                        .withRequired(true)
                        .build()
                )
                .build();
    }
}
