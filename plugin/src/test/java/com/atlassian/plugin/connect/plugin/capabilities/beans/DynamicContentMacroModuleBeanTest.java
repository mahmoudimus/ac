package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroCategory;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterType;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readAddonTestFile;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
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
                        .withKey("")
                        .withUrl("/my-macro")
                        .withAliases("some-alias")
                        .withBodyType(MacroBodyType.PLAIN_TEXT)
                        .withOutputType(MacroOutputType.BLOCK)
                        .withCategories(MacroCategory.HIDDEN)
                        .withDescription(new I18nProperty("Some Description", "some.macro.desc"))
                        .withDocumentationUrl("http://docs.example.com/macro")
                        .withFeatured(true)
                        .withWidth(100)
                        .withHeight(50)
                        .withIcon(newIconBean().withUrl("/mymacro/icon.png").withHeight(80).withWidth(80).build())
                        .withParameters(newMacroParameterBean()
                                .withName("Parameter Name")
                                .withType(MacroParameterType.STRING)
                                .withDefaultValue("default")
                                .withMultiple(false)
                                .withRequired(true)
                                .withValues("paramValue1", "paramValue2")
                                .withAliases("paramAlias1")
                                .build()
                        )
                        .build()
                )
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.OAUTH).withSharedKey("S0m3Publ1cK3y").build())
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("dynamicContentMacroAddon.json");
    }

}
