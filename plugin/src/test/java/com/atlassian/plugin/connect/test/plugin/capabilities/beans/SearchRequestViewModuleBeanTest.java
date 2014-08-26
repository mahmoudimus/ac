package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.JiraConfluenceConnectModulesGsonFactory;
import com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class SearchRequestViewModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        ConnectAddonBean bean = createBean();
        Gson gson = JiraConfluenceConnectModulesGsonFactory.getGson();
        String json = gson.toJson(bean, ConnectAddonBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = JiraConfluenceConnectModulesGsonFactory.getGson();
        ConnectAddonBean<JiraConfluenceModuleList> deserializedBean = JiraConfluenceConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);
        ConnectAddonBean bean = createBean();

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        ConnectAddonBean originalBean = createBean();
        Gson gson = JiraConfluenceConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, ConnectAddonBean.class);
        ConnectAddonBean<JiraConfluenceModuleList> deserializedBean = JiraConfluenceConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);

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
                .withModule("jiraSearchRequestViews", SearchRequestViewModuleBean.newSearchRequestViewModuleBean()
                        .withName(new I18nProperty("My Search Request View", "my.searchRequestView"))
                        .withKey("jira-search-request-view")
                        .withDescription(new I18nProperty("My description", "my.searchRequestView.desc"))
                        .withUrl("/search-request.csv")
                        .withWeight(10)
                        .withParam("delimiter", ",")
                        .withConditions(newSingleConditionBean().withCondition("user_is_logged_in").build())
                        .build())
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.OAUTH).withPublicKey("S0m3Publ1cK3y").build())
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("searchRequestViewAddon.json");
    }
}
