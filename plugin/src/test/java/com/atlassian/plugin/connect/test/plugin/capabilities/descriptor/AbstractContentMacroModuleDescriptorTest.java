package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.confluence.macro.browser.beans.MacroIcon;
import com.atlassian.confluence.macro.browser.beans.MacroParameter;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.confluence.util.i18n.DocumentationBean;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.LinkBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.spring.container.ContainerContext;
import com.atlassian.spring.container.ContainerManager;
import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


public abstract class AbstractContentMacroModuleDescriptorTest<B extends BaseContentMacroModuleBean, T extends BaseContentMacroModuleBeanBuilder<T, B>>
{
    @Mock
    protected ContainerContext containerContext;

    protected Plugin plugin = new PluginForTests("my-plugin", "My Plugin");
    protected XhtmlMacroModuleDescriptor descriptor;

    protected abstract XhtmlMacroModuleDescriptor createModuleDescriptorForTest();

    protected abstract T newContentMacroModuleBeanBuilder();

    @Before
    public void beforeEachTest() throws Exception
    {
        setupContainer();
        this.descriptor = createModuleDescriptorForTest();
        this.descriptor.enabled();
    }

    private void setupContainer() {
        MockitoAnnotations.initMocks(this);
        when(containerContext.isSetup()).thenReturn(true);
        when(containerContext.getComponent("docBean")).thenReturn(new MockDocBean());
        ContainerManager.getInstance().setContainerContext(containerContext);
    }

    @Test
    public void verifyPluginKeyIsCorrect() throws Exception
    {
        assertThat(descriptor.getPluginKey(), is("my-plugin"));
    }

    @Test
    public void verifyKeyIsCorrect()
    {
        assertThat(descriptor.getKey(), is("macro-the-macro-name"));
    }

    @Test
    public void verifyCompleteKeyIsCorrect()
    {
        assertThat(descriptor.getCompleteKey(), is("my-plugin:macro-the-macro-name"));
    }

    @Test
    public void verifyNameIsSet() throws Exception
    {
        assertThat(descriptor.getName(), is("the-macro-name"));
    }

    @Test
    public void verifyMacroNameIsSet() throws Exception
    {
        assertThat(descriptor.getMacroMetadata().getMacroName(), is("the-macro-name"));
    }

    @Test
    public void verifyMacroTitleIsSet() throws Exception
    {
        assertThat(descriptor.getMacroMetadata().getTitle().getKey(), is("my-plugin.the-macro-name.label"));
    }

    @Test
    public void verifyMacroDescriptionIsSet() throws Exception
    {
        assertThat(descriptor.getMacroMetadata().getDescription().getKey(), is("my-plugin.the-macro-name.desc"));
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
                "media",
                "confluence_content"));
    }

    @Test
    public void verifyIconAttributes() throws Exception
    {
        MacroIcon icon = descriptor.getMacroMetadata().getIcon();
        assertThat(icon, allOf(
                hasProperty("width", is(80)),
                hasProperty("height", is(80)),
                hasProperty("location", is("http://www.example.com/assets/macro.png"))
        ));
    }

    @Test
    public void verifyParameters() throws Exception
    {
        List<MacroParameter> parameters = descriptor.getMacroMetadata().getFormDetails().getParameters();
        MacroParameter macroParameter = parameters.get(0);
        assertThat(macroParameter, allOf(
                hasProperty("defaultValue", is("default")),
                hasProperty("name", is("parametername")),
                hasProperty("type", hasToString("enum")),
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
    public void verifyParameterValue() throws Exception
    {
        List<MacroParameter> parameters = descriptor.getMacroMetadata().getFormDetails().getParameters();
        MacroParameter macroParameter = parameters.get(0);

        assertThat(macroParameter.getEnumValues(), contains("Parameter Value"));
    }

    @Test
    public void verifyParameterAlias() throws Exception
    {
        List<MacroParameter> parameters = descriptor.getMacroMetadata().getFormDetails().getParameters();
        MacroParameter macroParameter = parameters.get(0);

        assertThat(macroParameter.getAliases(), contains("Parameter Alias"));
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

    @Test
    public void verifyIsHidden() throws Exception
    {
        assertThat(descriptor.getMacroMetadata().isHidden(), is(true));
    }

    protected T createBeanBuilder()
    {
        return newContentMacroModuleBeanBuilder()
                .withName(new I18nProperty("The Macro Name", "macro.name.key"))
                .withKey("the-macro-name")
                .withUrl("/my-macro")
                .withAliases("alias1", "alias2")
                .withBodyType(MacroBodyType.PLAIN_TEXT)
                .withOutputType(MacroOutputType.BLOCK)
                .withCategories("media", "confluence_content")
                .withDescription(new I18nProperty("The Macro Description", "macro.desc.key"))
                .withHidden(true)
                .withDocumentation(LinkBean.newLinkBean()
                        .withUrl("http://docs.example.com/macro")
                        .withTitle("Doc Title")
                        .withAltText("Doc Alt Text")
                        .build()
                )
                .withIcon(newIconBean().withUrl("/assets/macro.png").withHeight(80).withWidth(80).build())
                .withParameters(newMacroParameterBean()
                        .withIdentifier("parametername")
                        .withType("enum")
                        .withDefaultValue("default")
                        .withMultiple(false)
                        .withRequired(true)
                        .withValues("Parameter Value")
                        .withAliases("Parameter Alias")
                        .build()
                );
    }

    private static class MockDocBean implements DocumentationBean {

        @Override
        public String getLink(String docLink) {
            return docLink;
        }

        @Override
        public String getTitle(String docLink) {
            throw new NotImplementedException();
        }

        @Override
        public String getAlt(String docLink) {
            throw new NotImplementedException();
        }

        @Override
        public boolean isLocal(String docLink) {
            throw new NotImplementedException();
        }

        @Override
        public boolean exists(String docLink) {
            throw new NotImplementedException();
        }
    }

}
