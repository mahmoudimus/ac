package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readCapabilitiesTestFile;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.SearchRequestViewCapabilityBean.newSearchRequestViewCapabilityBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class SearchRequestViewCapabilityBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        ConnectAddonBean bean = createBean();
        Gson gson = CapabilitiesGsonFactory.getGson();
        String json = gson.toJson(bean, ConnectAddonBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);
        ConnectAddonBean bean = createBean();

        assertThat(deserializedBean, sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        ConnectAddonBean originalBean = createBean();
        Gson gson = CapabilitiesGsonFactory.getGson();
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
                .withCapability("jiraSearchRequestViews", newSearchRequestViewCapabilityBean()
                        .withName(new I18nProperty("My Search Request View", "my.searchRequestView"))
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
        return readCapabilitiesTestFile("searchRequestViewAddon.json");
    }
}
