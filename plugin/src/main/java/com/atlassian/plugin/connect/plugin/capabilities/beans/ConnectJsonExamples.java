package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;

@SuppressWarnings ("UnusedDeclaration")
public class ConnectJsonExamples
{
    private static final Gson gson = ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create();

    public static final String ADDON_EXAMPLE = createAddonExample();
    public static final String DYNAMIC_MACRO_EXAMPLE = createDynamicMacroExample();

    public static final String PARAMS_EXAMPLE = getParamsExample();

    private static String createAddonExample()
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

    private static String createDynamicMacroExample()
    {
        DynamicContentMacroModuleBean macroModuleBean = newDynamicContentMacroModuleBean()
                .withName(new I18nProperty("Maps", ""))
                .withUrl("/render-map")
                .withAliases("map")
                .withBodyType(MacroBodyType.NONE)
                .withOutputType(MacroOutputType.BLOCK)
                .withCategories("visuals")
                .withDescription(new I18nProperty("Shows a configurable map", ""))
                .withDocumentation(LinkBean.newLinkBean()
                        .withUrl("http://docs.example.com/addons/maps")
                        .build()
                )
                .withFeatured(true)
                .withWidth(200)
                .withHeight(200)
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(80).withWidth(80).build())
                .withParameters(newMacroParameterBean()
                        .withIdentifier("view")
                        .withName(new I18nProperty("Map View", ""))
                        .withDescription(new I18nProperty("Allows switching between view types", ""))
                        .withType("enum")
                        .withDefaultValue("Map")
                        .withMultiple(false)
                        .withRequired(true)
                        .withValues("Map", "Satellite")
                        .build()
                )
                .build();

        return gson.toJson(macroModuleBean);
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
