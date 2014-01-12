package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.*;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readAddonTestFile;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.ImagePlaceholderBean.newImagePlaceholderBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroEditorBean.newMacroEditorBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class StaticContentMacroModuleBeanTest
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

        assertThat(deserializedBean, sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        ConnectAddonBean originalBean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, ConnectAddonBean.class);
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(originalBean));
    }

    @Test
    public void verifyDefaultMethod()
    {
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = "{}";
        StaticContentMacroModuleBean deserializedBean = gson.fromJson(json, StaticContentMacroModuleBean.class);

        assertThat(deserializedBean.getMethod(), is(MacroHttpMethod.GET));
    }

    private static ConnectAddonBean createBean()
    {
        return newConnectAddonBean()
                .withName("My Add-On")
                .withKey("my-add-on")
                .withVersion("2.0")
                .withBaseurl("http://www.example.com")
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withModule("staticContentMacros", newStaticContentMacroModuleBean()
                        .withName(new I18nProperty("Some Macro", "some.macro.name"))
                        .withKey("")
                        .withUrl("/my-macro")
                        .withAliases("some-alias")
                        .withMethod(MacroHttpMethod.POST)
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
                        .build()
                )
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("staticContentMacroAddon.json");
    }

}
