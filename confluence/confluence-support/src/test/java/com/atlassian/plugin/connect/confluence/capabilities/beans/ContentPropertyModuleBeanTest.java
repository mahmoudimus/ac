package com.atlassian.plugin.connect.confluence.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.testsupport.util.matcher.SameDeepPropertyValuesAs;
import com.atlassian.plugin.connect.util.io.TestFileReader;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class ContentPropertyModuleBeanTest
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
                .withModule("confluenceContentProperties", ConnectJsonExamples.createContentPropertyExampleBean())
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return TestFileReader.readAddonTestFile("contentPropertyAddon.json");
    }

}
