package com.atlassian.plugin.connect.confluence.property;

import com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.testsupport.util.matcher.SameDeepPropertyValuesAs;
import com.atlassian.plugin.connect.util.io.TestFileReader;
import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

import java.io.IOException;

public class ContentPropertyModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        ContentPropertyModuleBean bean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(bean, ContentPropertyModuleBean.class);
        String expectedJson = readTestFile();

        Assert.assertThat(json, Matchers.is(SameJSONAs.sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = ConnectModulesGsonFactory.getGson();
        ContentPropertyModuleBean deserializedBean = gson.fromJson(json, ContentPropertyModuleBean.class);
        ContentPropertyModuleBean bean = createBean();

        Assert.assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        ContentPropertyModuleBean originalBean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, ContentPropertyModuleBean.class);
        ContentPropertyModuleBean deserializedBean = gson.fromJson(json, ContentPropertyModuleBean.class);

        Assert.assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(originalBean));
    }

    private static ContentPropertyModuleBean createBean()
    {
        return ConnectJsonExamples.createContentPropertyExampleBean();
    }

    private static String readTestFile() throws IOException
    {
        return TestFileReader.readAddonTestFile("contentProperty.json");
    }
}
