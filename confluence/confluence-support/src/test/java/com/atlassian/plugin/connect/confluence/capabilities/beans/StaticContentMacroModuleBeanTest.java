package com.atlassian.plugin.connect.confluence.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.LinkBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean.newImagePlaceholderBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean.newMacroEditorBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.testsupport.util.matcher.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class StaticContentMacroModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        StaticContentMacroModuleBean bean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(bean, StaticContentMacroModuleBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = ConnectModulesGsonFactory.getGson();
        StaticContentMacroModuleBean deserializedBean = gson.fromJson(json, StaticContentMacroModuleBean.class);
        StaticContentMacroModuleBean bean = createBean();

        assertThat(deserializedBean, sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        StaticContentMacroModuleBean originalBean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, StaticContentMacroModuleBean.class);
        StaticContentMacroModuleBean deserializedBean = gson.fromJson(json, StaticContentMacroModuleBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(originalBean));
    }

    private static StaticContentMacroModuleBean createBean()
    {
        return newStaticContentMacroModuleBean()
            .withName(new I18nProperty("Some Macro", "some.macro.name"))
            .withKey("static-content-macro")
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
            .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("staticContentMacroAddon.json");
    }

}
