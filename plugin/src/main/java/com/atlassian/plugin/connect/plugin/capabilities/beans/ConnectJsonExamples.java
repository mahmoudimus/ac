package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroEditorBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean.newConfigurePageBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelModuleBean.newWebPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean.newLinkBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroEditorBean.newMacroEditorBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;

@SuppressWarnings ("UnusedDeclaration")
public class ConnectJsonExamples
{
    private static final Gson gson = ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create();

    public static final String ADDON_EXAMPLE = createAddonExample();
    public static final String ADDON_COMPLETE_EXAMPLE = createAddonCompleteExample();
    public static final String AUTHENTICATION_EXAMPLE = createAuthenticationExample();
    public static final String COMPONENT_TAB_PANEL_EXAMPLE = createComponentTabPanelExample();
    public static final String CONFIGURE_PAGE_EXAMPLE = createConfigurePageExample();
    public static final String DYNAMIC_MACRO_EXAMPLE = createDynamicMacroExample();
    public static final String I18N_EXAMPLE = createI18nExample();
    public static final String ICON_EXAMPLE = createIconExample();
    public static final String LINK_EXAMPLE = createLinkExample();
    public static final String MACRO_EDITOR_EXAMPLE = createMacroEditorExample();
    public static final String PAGE_EXAMPLE = createPageExample();
    public static final String PANEL_LAYOUT_EXAMPLE = createPanelLayoutExample();
    public static final String PARAMS_EXAMPLE = createParamsExample();
    public static final String POST_FUNCTION_EXAMPLE = createPostFunctionExample();
    public static final String PRJ_ADMIN_PAGE_EXAMPLE = createProjectAdminPageExample();
    public static final String SEARCH_VIEW_EXAMPLE = createSearchViewExample();
    public static final String SINGLE_CONDITION_EXAMPLE = createSingleConditionExample();
    public static final String STATIC_MACRO_EXAMPLE = createStaticMacroExample();
    public static final String URL_EXAMPLE = createUrlExample();
    public static final String VENDOR_EXAMPLE = createVendorExample();
    public static final String WEBHOOK_EXAMPLE = createWebhookExample();
    public static final String WEBITEM_EXAMPLE = createWebItemExample();
    public static final String WEBITEM_TARGET_EXAMPLE = createWebitemTargetExample();
    public static final String WEBPANEL_EXAMPLE = createWebPanelExample();


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
                .withModules("webItems", newWebItemBean().build())
                .build();

        return gson.toJson(addonBean);
    }

    private static String createAddonCompleteExample()
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
                .withModules("jiraWorkflowPostFunctions", newWorkflowPostFunctionBean().withCreate(new UrlBean("/create")).build())
                .withModules("jiraSearchRequestViews", newSearchRequestViewModuleBean().withUrl("/searchRequest").build())
                .withModules("profilePages", newPageBean().withUrl("my-confluence-profile-page").build())
                .withModules("dynamicContentMacros", newDynamicContentMacroModuleBean().withUrl("/dynamic-macro").build())
                .withModules("staticContentMacros", newStaticContentMacroModuleBean().withUrl("/static-macro").build())
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

    private static String createProjectAdminPageExample()
    {
        ConnectProjectAdminTabPanelModuleBean pageModuleBean = newProjectAdminTabPanelBean()
                .withName(new I18nProperty("Admin Panel", ""))
                .withUrl("/my-admin-panel")
                .withLocation("projectgroup4")
                .build();

        return gson.toJson(createModuleArray("jiraProjectAdminTabPanels",pageModuleBean));
    }

    private static String createConfigurePageExample()
    {
        ConnectPageModuleBean ConnectPageModuleBean = newPageBean()
                .withName(new I18nProperty("Configure Page", ""))
                .withUrl("/configure-world")
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(80).withWidth(80).build())
                .build();

        return gson.toJson(ConnectPageModuleBean);
    }

    private static String createWebhookExample()
    {
        WebHookModuleBean bean = newWebHookBean()
                .withEvent("jira:issuecreated")
                .withUrl("/issue-created")
                .build();

        return gson.toJson(createModuleArray("webhooks",bean));
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

    private static String createPostFunctionExample()
    {
        WorkflowPostFunctionModuleBean bean = newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Function", "my.function.name"))
                .withDescription(new I18nProperty("My Description","my.function.desc"))
                .withTriggered(new UrlBean("/triggered"))
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withView(new UrlBean("/view"))
                .build();

        return gson.toJson(createModuleArray("jiraWorkflowPostFunctions",bean));
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

    public static String createSearchViewExample()
    {
        SearchRequestViewModuleBean bean = newSearchRequestViewModuleBean()
                .withName(new I18nProperty("My Search View", "my.search.view"))
                .withUrl("/search-request.csv")
                .withWeight(100)
                .build();

        return gson.toJson(createModuleArray("jiraSearchRequestViews",bean));
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
                .withDocumentation(newLinkBean()
                        .withUrl("http://docs.example.com/addons/maps")
                        .build()
                )
                .withFeatured(true)
                .withWidth("200px")
                .withHeight("200px")
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

    private static String createStaticMacroExample()
    {
        StaticContentMacroModuleBean macroModuleBean = newStaticContentMacroModuleBean()
                .withName(new I18nProperty("Maps", ""))
                .withUrl("/render-map")
                .withAliases("map")
                .withBodyType(MacroBodyType.NONE)
                .withOutputType(MacroOutputType.BLOCK)
                .withCategories("visuals")
                .withDescription(new I18nProperty("Shows a configurable map", ""))
                .withDocumentation(newLinkBean()
                        .withUrl("http://docs.example.com/addons/maps")
                        .build()
                )
                .withFeatured(true)
                .withWidth("200px")
                .withHeight("200px")
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

        return gson.toJson(createModuleArray("staticContentMacros",macroModuleBean));
    }

    private static String createI18nExample()
    {
        I18nProperty bean = new I18nProperty("jim","my.name.is.jim");
        return gson.toJson(createModuleObject("name",bean));
    }

    private static String createIconExample()
    {
        IconBean bean = newIconBean().withUrl("/my-icon.png").withWidth(16).withHeight(16).build();
        return gson.toJson(createModuleObject("icon",bean));
    }

    private static String createWebitemTargetExample()
    {
        WebItemTargetBean bean = newWebItemTargetBean()
                .withType(WebItemTargetType.page).build();
        
        return gson.toJson(createModuleObject("target",bean));
    }

    private static String createLinkExample()
    {
        LinkBean bean = newLinkBean().withUrl("/go-somewhere").withAltText("somewhere").withTitle("Go Somewhere").build();
        return gson.toJson(createModuleObject("link",bean));
    }

    private static String createSingleConditionExample()
    {
        SingleConditionBean bean = newSingleConditionBean().withCondition("user_is_logged_in").build();
        return gson.toJson(createModuleObject("condition",bean));
    }

    private static String createUrlExample()
    {
        UrlBean bean = new UrlBean("/my-url");
        return gson.toJson(createModuleObject("url",bean));
    }

    private static String createVendorExample()
    {
        VendorBean bean = newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build();
        return gson.toJson(createModuleObject("vendor",bean));
    }

    private static String createPanelLayoutExample()
    {
        WebPanelLayout bean = new WebPanelLayout("100","200");
        return gson.toJson(createModuleObject("layout", bean));
    }

    private static String createAuthenticationExample()
    {
        AuthenticationBean bean = newAuthenticationBean().withType(AuthenticationType.JWT).build();
        return gson.toJson(createModuleObject("authentication",bean));
    }

    private static String createParamsExample()
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

    private static JsonObject createModuleObject(String name, Object bean)
    {
        JsonObject obj = new JsonObject();
        obj.add(name, gson.toJsonTree(bean));

        return obj;
    }

    private static String createMacroEditorExample()
    {
        MacroEditorBean macroEditorBean = newMacroEditorBean()
                .withUrl("/generate_md")
                .withInsertTitle(new I18nProperty("Insert MarkDown", "macro.md.insert"))
                .withEditTitle(new I18nProperty("Edit MarkDown", "macro.md.edit"))
                .withHeight("300px")
                .withWidth("400px")
                .build();

        return gson.toJson(macroEditorBean);
    }
}
