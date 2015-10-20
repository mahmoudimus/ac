package com.atlassian.plugin.connect.jira.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.testsupport.util.matcher.SameDeepPropertyValuesAs;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class SearchRequestViewModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        SearchRequestViewModuleBean bean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(bean, SearchRequestViewModuleBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = ConnectModulesGsonFactory.getGson();
        SearchRequestViewModuleBean deserializedBean = gson.fromJson(json, SearchRequestViewModuleBean.class);
        SearchRequestViewModuleBean bean = createBean();

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        SearchRequestViewModuleBean originalBean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, SearchRequestViewModuleBean.class);
        SearchRequestViewModuleBean deserializedBean = gson.fromJson(json, SearchRequestViewModuleBean.class);

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(originalBean));
    }

    private static SearchRequestViewModuleBean createBean()
    {
        return SearchRequestViewModuleBean.newSearchRequestViewModuleBean()
            .withName(new I18nProperty("My Search Request View", "my.searchRequestView"))
            .withKey("jira-search-request-view")
            .withDescription(new I18nProperty("My description", "my.searchRequestView.desc"))
            .withUrl("/search-request.csv")
            .withWeight(10)
            .withParam("delimiter", ",")
            .withConditions(newSingleConditionBean().withCondition("user_is_logged_in").build())
            .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("searchRequestViewAddon.json");
    }
}
