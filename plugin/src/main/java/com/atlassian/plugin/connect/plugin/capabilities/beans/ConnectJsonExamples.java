package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;

public class ConnectJsonExamples
{
    private static final Gson gson = ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create();

    public static final String ADDON_EXAMPLE = getAddonExample();

    public static final String PARAMS_EXAMPLE = getParamsExample();

    private static String getAddonExample()
    {
        ConnectAddonBean addonBean = newConnectAddonBean()
                .withKey("my-addon-key")
                .withName("My Connect Addon")
                .withDescription("A connect addon that does something")
                .withVendor(newVendorBean().withName("My Company").withUrl("http://www.example.com").build())
                .withBaseurl("http://www.example.com/connect/jira")
                .withLinks(ImmutableMap.builder().put("self", "http://www.example.com/connect/jira").build())
                .build();


        return gson.toJson(addonBean);
    }

    private static String getParamsExample()
    {
        Map<String, String> params = new HashMap<String, String>(2);
        params.put("myCustomProperty", "myValue");
        params.put("someOtherProperty", "someValue");

        
        JsonObject obj = new JsonObject();
        obj.add("params", gson.toJsonTree(params));

        return gson.toJson(obj);
    }

}
