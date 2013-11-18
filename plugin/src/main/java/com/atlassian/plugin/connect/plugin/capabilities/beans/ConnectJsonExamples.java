package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static com.google.common.collect.Maps.newHashMap;

public class ConnectJsonExamples
{
    public static final String ADDON_EXAMPLE = getAddonExample();

    private static String getAddonExample()
    {
        Gson gson = CapabilitiesGsonFactory.getGsonBuilder().setPrettyPrinting().create();
        ConnectAddonBean addonBean = newConnectAddonBean()
                .withKey("my-addon-key")
                .withName("My Connect Addon")
                .withDescription("A connect addon that does something")
                .withVendor(newVendorBean().withName("My Company").withUrl("http://www.example.com").build())
                .withBaseurl("http://www.example.com/connect/jira")
                .withLinks(ImmutableMap.builder().put("self","http://www.example.com/connect/jira").build())
                .build();


        return gson.toJson(addonBean);
    }

    
}
