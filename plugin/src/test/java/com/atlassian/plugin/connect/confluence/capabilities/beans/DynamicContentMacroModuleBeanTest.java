package com.atlassian.plugin.connect.confluence.capabilities.beans;

import com.atlassian.plugin.connect.testsupport.util.matcher.SameDeepPropertyValuesAs;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.LinkBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;
import com.atlassian.plugin.connect.modules.beans.nested.MatcherBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.util.io.TestFileReader;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;

import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean.newImagePlaceholderBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean.newMacroEditorBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class DynamicContentMacroModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        ConnectAddonBean bean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(bean, ConnectAddonBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);
        ConnectAddonBean bean = createBean();

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        ConnectAddonBean originalBean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, ConnectAddonBean.class);
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(originalBean));
    }

    private static ConnectAddonBean createBean()
    {
        return newConnectAddonBean()
                .withName("My Add-On")
                .withKey("my-add-on")
                .withVersion("2.0")
                .withBaseurl("http://www.example.com")
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withModule("dynamicContentMacros", newDynamicContentMacroModuleBean()
                                .withName(new I18nProperty("Some Macro", "some.macro.name"))
                                .withKey("dynamic-content-macro")
                                .withUrl("/my-macro")
                                .withAliases("some-alias")
                                .withBodyType(MacroBodyType.PLAIN_TEXT)
                                .withOutputType(MacroOutputType.BLOCK)
                                .withCategories("hidden-macros")
                                .withDescription(new I18nProperty("Some Description", "some.macro.desc"))
                                .withDocumentation(LinkBean.newLinkBean()
                                                .withUrl("http://docs.example.com/macro")
                                                .withTitle("Doc Title")
                                                .withAltText("Doc Alt Text")
                                                .build()
                                )
                                .withFeatured(true)
                                .withWidth("100px")
                                .withHeight("50px")
                                .withIcon(newIconBean().withUrl("/mymacro/icon.png").withHeight(80).withWidth(80).build())
                                .withParameters(newMacroParameterBean()
                                                .withIdentifier("myparam")
                                                .withName(new I18nProperty("Some Name", "name.key"))
                                                .withDescription(new I18nProperty("Some Description", "desc.key"))
                                                .withType("enum")
                                                .withDefaultValue("default")
                                                .withMultiple(false)
                                                .withRequired(true)
                                                .withValues("paramValue1", "paramValue2")
                                                .withAliases("paramAlias1")
                                                .build()
                                )
                                .withEditor(newMacroEditorBean()
                                                .withUrl("/my-macro-editor")
                                                .withEditTitle(new I18nProperty("Edit Title", "edit.title.key"))
                                                .withInsertTitle(new I18nProperty("Insert Title", "insert.title.key"))
                                                .withHeight("100px")
                                                .withWidth("200px")
                                                .build()
                                )
                                .withImagePlaceholder(newImagePlaceholderBean()
                                                .withUrl("images/placeholder.png")
                                                .withWidth(100)
                                                .withHeight(25)
                                                .withApplyChrome(true)
                                                .build()
                                )
                                .withRenderModes(MacroRenderModesBean.newMacroRenderModesBean()
                                        .withDefaultfallback(
                                                EmbeddedStaticContentMacroBean.newEmbeddedStaticContentMacroModuleBean()
                                                        .withUrl("/render-map-default")
                                                        .build())
                                        .withPdf(
                                                EmbeddedStaticContentMacroBean.newEmbeddedStaticContentMacroModuleBean()
                                                        .withUrl("/render-map-pdf")
                                                        .build())
                                        .build())
                                .withAutoconvert(AutoconvertBean.newAutoconvertBean()
                                        .withUrlParameter("url")
                                        .withMatchers(
                                                MatcherBean.newMatcherBean()
                                                        .withPattern("/docs/google.com/document/d/{fileId}/edit")
                                                        .build())
                                        .build())
                                .build()
                )
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return TestFileReader.readAddonTestFile("dynamicContentMacroAddon.json");
    }

}
