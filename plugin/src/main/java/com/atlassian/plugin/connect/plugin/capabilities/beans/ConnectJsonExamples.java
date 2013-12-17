package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.*;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean.newConfigurePageBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.SearchRequestViewModuleBean.newSearchRequestViewModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebHookModuleBean.newWebHookBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelModuleBean.newWebPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;

@SuppressWarnings ("UnusedDeclaration")
public class ConnectJsonExamples
{
    private static final Gson gson = ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create();

    public static final String ADDON_EXAMPLE = createAddonExample();
    public static final String KITCHEN_SINK_EXAMPLE = createKitchenSinkExample();
    public static final String DYNAMIC_MACRO_EXAMPLE = createDynamicMacroExample();
    public static final String PAGE_EXAMPLE = createPageExample();
    public static final String CONFIGURE_PAGE_EXAMPLE = createConfigurePageExample();
    public static final String WEBITEM_EXAMPLE = createWebItemExample();
    public static final String WEBPANEL_EXAMPLE = createWebPanelExample();
    public static final String COMPONENT_TAB_PANEL_EXAMPLE = createComponentTabPanelExample();


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
                .withAuthentication(newAuthenticationBean().build())
                .withLicensing(true)
                .withLifecycle(newLifecycleBean().withInstalled("/installed").withUninstalled("/uninstalled").build())
                .withModules("webItems",newWebItemBean().build())
                .build();

        return gson.toJson(addonBean);
    }

    private static String createKitchenSinkExample()
    {
        ConnectAddonBean addonBean = newConnectAddonBean()
                .withKey("my-addon-key")
                .withName("My Connect Addon")
                .withDescription("A connect addon that does something")
                .withVendor(newVendorBean().withName("My Company").withUrl("http://www.example.com").build())
                .withBaseurl("http://www.example.com/connect/jira")
                .withLinks(ImmutableMap.builder().put("self", "http://www.example.com/connect/jira").build())
                .withAuthentication(newAuthenticationBean().build())
                .withLicensing(true)
                .withLifecycle(newLifecycleBean().withInstalled("/installed").withUninstalled("/uninstalled").build())
                .withModules("webItems",newWebItemBean().withLink("/my-web-item").withLocation("system.preset.filters").build())
                .withModules("webPanels",newWebPanelBean().withLocation("com.atlassian.jira.plugin.headernav.left.context").withUrl("/my-web-panel").build())
                .withModules("generalPages",newPageBean().withUrl("my-general-page").build())
                .withModules("adminPages",newPageBean().withUrl("my-admin-page").build())
                .withModules("configurePages", newConfigurePageBean().withUrl("my-configure-page").build())
                .withModules("webhooks", newWebHookBean().withEvent("jira:issue_created").withUrl("/issue-created").build())
                .withModules("jiraComponentTabPanels", newTabPanelBean().withUrl("my-component-tab-panel").build())
                .withModules("jiraIssueTabPanels",newTabPanelBean().withUrl("my-issue-tab-panel").build())
                .withModules("jiraProjectAdminTabPanels",newTabPanelBean().withUrl("my-admin-tab-panel").build())
                .withModules("jiraProjectTabPanels",newTabPanelBean().withUrl("my-project-tab-panel").build())
                .withModules("jiraVersionTabPanels",newTabPanelBean().withUrl("my-version-tab-panel").build())
                .withModules("jiraProfileTabPanels",newTabPanelBean().withUrl("my-profile-tab-panel").build())
                .withModules("jiraWorkflowPostFunctions",newWorkflowPostFunctionBean().withCreate(new UrlBean("/create")).build())
                .withModules("jiraSearchRequestViews",newSearchRequestViewModuleBean().withUrl("/searchRequest").build())
                .withModules("profilePages",newPageBean().withUrl("my-confluence-profile-page").build())
                .withModules("dynamicContentMacros",newDynamicContentMacroModuleBean().withUrl("/dynamic-macro").build())
                .withModules("staticContentMacros",newStaticContentMacroModuleBean().withUrl("/static-macro").build())
                .build();

        return gson.toJson(addonBean);
    }

    private static String createPageExample()
    {
        ConnectPageModuleBean pageModuleBean = newPageBean()
                .withName(new I18nProperty("General Page", ""))
                .withUrl("/hello-world")
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(80).withWidth(80).build())
                .build();

        return gson.toJson(createModuleArray("generalPages",pageModuleBean));
    }


    private static String createConfigurePageExample()
    {
        ConfigurePageModuleBean configurePageModuleBean = newConfigurePageBean()
                .withName(new I18nProperty("Configure Page", ""))
                .withUrl("/configure-world")
                .setAsDefault()
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(80).withWidth(80).build())
                .build();

        return gson.toJson(configurePageModuleBean);
    }


    private static String createWebItemExample()
    {
        WebItemModuleBean webItemModuleBean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", ""))
                .withLink("/my-web-item")
                .withLocation("system.preset.filters")
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(16).withWidth(16).build())
                .withStyleClasses("webitem", "system-present-webitem")
                .withTooltip(new I18nProperty("Example tooltip", ""))
                .withWeight(200)
                .build();

        return gson.toJson(createModuleArray("webItems",webItemModuleBean));
    }


    private static String createWebPanelExample()
    {
        WebPanelModuleBean webPanelModuleBean = newWebPanelBean()
                .withName(new I18nProperty("My Web Panel", ""))
                .withUrl("http://www.example.com/web-panel")
                .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                .withLayout(new WebPanelLayout("10px", "100%"))
                .withWeight(50)
                .build();

        return gson.toJson(createModuleArray("webPanels",webPanelModuleBean));
    }


    public static String createComponentTabPanelExample()
    {
        ConnectTabPanelModuleBean bean = newTabPanelBean()
                        .withName(new I18nProperty("My Component Tab Page", ""))
                        .withUrl("/my-component-tab")
                        .withWeight(100)
                        .build();

        return gson.toJson(createModuleArray("jiraComponentTabPanels",bean));
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

        return gson.toJson(createModuleArray("dynamicContentMacros",macroModuleBean));
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
    
    private static JsonObject createModuleArray(String name, ModuleBean bean)
    {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(gson.toJsonTree(bean));
        obj.add("generalPages", arr);
        
        return obj;
    }
}
