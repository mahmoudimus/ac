package com.atlassian.plugin.connect.modules.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexType;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.modules.beans.nested.LinkBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.InlineDialogOptions;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ContentPropertyIndexSchemaModuleBean.newContentPropertyIndexSchemaModuleBean;
import static com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean.newEntityPropertyModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean.newImagePlaceholderBean;
import static com.atlassian.plugin.connect.modules.beans.nested.LinkBean.newLinkBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean.newMacroEditorBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;

@SuppressWarnings ("UnusedDeclaration")
public class ConnectJsonExamples
{
    private static final Gson gson = ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create();

    public static final String ADDON_COMPLETE_EXAMPLE = createAddonCompleteExample();
    public static final String ADDON_EXAMPLE = createAddonExample();
    public static final String AUTHENTICATION_EXAMPLE = createAuthenticationExample();
    public static final String COMPONENT_TAB_PANEL_EXAMPLE = createComponentTabPanelExample();
    public static final String COMPOSITE_CONDITION_EXAMPLE = createCompositeConditionExample();
    public static final String DYNAMIC_MACRO_EXAMPLE = createDynamicMacroExample();
    public static final String ENTITY_PROPERTY_EXAMPLE = createEntityPropertyExample();
    public static final String ENTITY_PROPERTY_INDEX_EXTRACTION_CONFIGURATION_EXAMPLE = createEntityPropertyIndexExtractionConfigurationExample();
    public static final String ENTITY_PROPERTY_INDEX_KEY_CONFIGURATION_EXAMPLE = createEntityPropertyIndexKeyConfigurationExample();
    public static final String I18N_EXAMPLE = createI18nExample();
    public static final String ICON_EXAMPLE = createIconExample();
    public static final String IMAGE_PLACEHOLDER_EXAMPLE = createImagePlaceholderExample();
    public static final String LIFECYCLE_EXAMPLE = createLifecycleExample();
    public static final String LINK_EXAMPLE = createLinkExample();
    public static final String LINKS_EXAMPLE = createLinksExample();
    public static final String MACRO_EDITOR_EXAMPLE = createMacroEditorExample();
    public static final String MACRO_PARAMS_EXAMPLE = createMacroParamsExample();
    public static final String PAGE_EXAMPLE = createPageExample();
    public static final String PANEL_LAYOUT_EXAMPLE = createPanelLayoutExample();
    public static final String PARAMS_EXAMPLE = createParamsExample();
    public static final String POST_FUNCTION_EXAMPLE = createPostFunctionExample();
    public static final String PRJ_ADMIN_PAGE_EXAMPLE = createProjectAdminPageExample();
    public static final String SCOPES_EXAMPLE = createScopesExample();
    public static final String SEARCH_VIEW_EXAMPLE = createSearchViewExample();
    public static final String SINGLE_CONDITION_EXAMPLE = createSingleConditionExample();
    public static final String SPACE_TOOLS_TAB_EXAMPLE = createSpaceToolsTabExample();
    public static final String STATIC_MACRO_EXAMPLE = createStaticMacroExample();
    public static final String URL_EXAMPLE = createUrlExample();
    public static final String VENDOR_EXAMPLE = createVendorExample();
    public static final String WEBHOOK_EXAMPLE = createWebhookExample();
    public static final String WEBITEM_EXAMPLE = createWebItemExample();
    public static final String WEBITEM_TARGET_INLINE_DIALOG_EXAMPLE = createWebitemTargetInlineDialogOptionsExample();
    public static final String WEBITEM_TARGET_DIALOG_EXAMPLE = createWebitemTargetDialogOptionsExample();
    public static final String WEBPANEL_EXAMPLE = createWebPanelExample();
    public static final String WEBSECTION_EXAMPLE = createWebSectionExample();
    public static final String BLUEPRINT_EXAMPLE = createBlueprintExample();
    public static final String BLUEPRINT_TEMPLATE_EXAMPLE = createBlueprintTemplateExample();


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
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").withUninstalled("/uninstalled").build())
                .withScopes(Sets.newHashSet(ScopeName.READ, ScopeName.WRITE))
                .withModules("webItems", WebItemModuleBean.newWebItemBean()
                                .withName(i18nProperty("Web Item"))
                                .withUrl("/my-web-item")
                                .withKey("my-web-item")
                                .withLocation("system.preset.filters").build()
                )
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
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").withUninstalled("/uninstalled").build())
                .withScopes(Sets.newHashSet(ScopeName.READ, ScopeName.WRITE))
                .withModules("webItems", WebItemModuleBean.newWebItemBean().withName(i18nProperty("Web Item")).withUrl("/my-web-item").withLocation("system.preset.filters").build())
                .withModules("webPanels", WebPanelModuleBean.newWebPanelBean().withName(i18nProperty("Web Panel")).withLocation("com.atlassian.jira.plugin.headernav.left.context").withUrl("/my-web-panel").build())
                .withModules("generalPages", ConnectPageModuleBean.newPageBean().withName(i18nProperty("General Page")).withUrl("my-general-page").build())
                .withModules("adminPages", ConnectPageModuleBean.newPageBean().withName(i18nProperty("Admin Page")).withUrl("my-admin-page").build())
                .withModules("configurePage", ConnectPageModuleBean.newPageBean().withName(i18nProperty("Config Page")).withUrl("my-configure-page").build())
                .withModules("webhooks", WebHookModuleBean.newWebHookBean().withEvent("jira:issue_created").withUrl("/issue-created").build())
                .withModules("jiraComponentTabPanels", ConnectTabPanelModuleBean.newTabPanelBean().withName(i18nProperty("Component Tab")).withUrl("my-component-tab-panel").build())
                .withModules("jiraIssueTabPanels", ConnectTabPanelModuleBean.newTabPanelBean().withName(i18nProperty("Issue Tab")).withUrl("my-issue-tab-panel").build())
                .withModules("jiraProjectAdminTabPanels", ConnectTabPanelModuleBean.newTabPanelBean().withUrl("my-admin-tab-panel").build())
                .withModules("jiraProjectTabPanels", ConnectTabPanelModuleBean.newTabPanelBean().withName(i18nProperty("Project Tab")).withUrl("my-project-tab-panel").build())
                .withModules("jiraVersionTabPanels", ConnectTabPanelModuleBean.newTabPanelBean().withName(i18nProperty("Version Tab")).withUrl("my-version-tab-panel").build())
                .withModules("jiraProfileTabPanels", ConnectTabPanelModuleBean.newTabPanelBean().withName(i18nProperty("Profile Tab")).withUrl("my-profile-tab-panel").build())
                .withModules("jiraWorkflowPostFunctions", WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean().withName(i18nProperty("Workflow Function")).withCreate(new UrlBean("/create")).build())
                .withModules("jiraSearchRequestViews", SearchRequestViewModuleBean.newSearchRequestViewModuleBean().withName(i18nProperty("Search View")).withUrl("/searchRequest").build())
                .withModules("profilePages", ConnectPageModuleBean.newPageBean().withName(i18nProperty("Profile Page")).withUrl("my-confluence-profile-page").build())
                .withModules("dynamicContentMacros", DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean().withName(i18nProperty("Dynamic Macro")).withUrl("/dynamic-macro").build())
                .withModules("staticContentMacros", StaticContentMacroModuleBean.newStaticContentMacroModuleBean().withName(i18nProperty("Static Macro")).withUrl("/static-macro").build())
                .withModules("spaceToolsTabs", SpaceToolsTabModuleBean.newSpaceToolsTabBean().withName(i18nProperty("Space Tools Tab")).withUrl("/space-tools").build())
                .build();

        return gson.toJson(addonBean);
    }

    private static I18nProperty i18nProperty(String name)
    {
        return new I18nProperty(name, null);
    }

    private static String createPageExample()
    {
        ConnectPageModuleBean pageModuleBean = ConnectPageModuleBean.newPageBean()
                .withName(new I18nProperty("General Page", ""))
                .withKey("page-key")
                .withUrl("/hello-world")
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(80).withWidth(80).build())
                .build();

        return gson.toJson(createModuleArray("generalPages", pageModuleBean));
    }

    private static String createProjectAdminPageExample()
    {
        ConnectProjectAdminTabPanelModuleBean pageModuleBean = ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean()
                .withName(new I18nProperty("Admin Panel", ""))
                .withKey("admin-panel")
                .withUrl("/my-admin-panel")
                .withLocation("projectgroup4")
                .build();

        return gson.toJson(createModuleArray("jiraProjectAdminTabPanels", pageModuleBean));
    }

    private static String createWebhookExample()
    {
        WebHookModuleBean bean = WebHookModuleBean.newWebHookBean()
                .withEvent("jira:issue_created")
                .withUrl("/issue-created")
                .build();

        return gson.toJson(createModuleArray("webhooks", bean));
    }

    private static String createWebItemExample()
    {
        WebItemModuleBean webItemModuleBean = WebItemModuleBean.newWebItemBean()
                .withName(new I18nProperty("My Web Item", ""))
                .withUrl("/my-web-item")
                .withKey("web-item-example")
                .withLocation("system.preset.filters")
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(16).withWidth(16).build())
                .withStyleClasses("webitem", "system-present-webitem")
                .withTooltip(new I18nProperty("Example tooltip", ""))
                .withWeight(200)
                .build();

        return gson.toJson(createModuleArray("webItems", webItemModuleBean));
    }

    private static String createPostFunctionExample()
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Function", "my.function.name"))
                .withKey("workflow-example")
                .withDescription(new I18nProperty("My Description", "my.function.desc"))
                .withTriggered(new UrlBean("/triggered"))
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withView(new UrlBean("/view"))
                .build();

        return gson.toJson(createModuleArray("jiraWorkflowPostFunctions", bean));
    }

    private static String createWebPanelExample()
    {
        WebPanelModuleBean webPanelModuleBean = WebPanelModuleBean.newWebPanelBean()
                .withName(new I18nProperty("My Web Panel", ""))
                .withUrl("/web-panel")
                .withKey("my-web-panel")
                .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                .withLayout(new WebPanelLayout("10px", "100%"))
                .withWeight(50)
                .build();

        return gson.toJson(createModuleArray("webPanels", webPanelModuleBean));
    }

    private static String createWebSectionExample()
    {
        WebSectionModuleBean webSectionModuleBean = WebSectionModuleBean.newWebSectionBean()
                .withName(new I18nProperty("My Web Section", ""))
                .withKey("my-web-section")
                .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                .withWeight(50)
                .build();

        return gson.toJson(createModuleArray("webSections", webSectionModuleBean));
    }

    private static String createBlueprintExample()
    {
        BlueprintModuleBean blueprintModuleBean = BlueprintModuleBean.newBlueprintModuleBean()
                .withName(new I18nProperty("Simple Remote Blueprint", ""))
                .withKey("remote-blueprint")
                .withTemplate(createBlueprintTemplateBean())
                .build();

        return gson.toJson(createModuleArray("blueprints", blueprintModuleBean));
    }

    private static String createBlueprintTemplateExample()
    {
        BlueprintTemplateBean blueprintTemplateBean = createBlueprintTemplateBean();
        return gson.toJson(createJsonObject("template", blueprintTemplateBean));
    }

    private static BlueprintTemplateBean createBlueprintTemplateBean()
    {
        return BlueprintTemplateBean.newBlueprintTemplateBeanBuilder()
                .withUrl("/blueprints/blueprint.xml")
                .build();
    }

    public static String createComponentTabPanelExample()
    {
        ConnectTabPanelModuleBean bean = ConnectTabPanelModuleBean.newTabPanelBean()
                .withName(new I18nProperty("My Component Tab Page", ""))
                .withKey("my-component-tab")
                .withUrl("/my-component-tab")
                .withWeight(100)
                .build();

        return gson.toJson(createModuleArray("jiraComponentTabPanels", bean));
    }

    private static String createScopesExample()
    {
        HashSet<ScopeName> scopeNames = Sets.newHashSet(ScopeName.READ, ScopeName.WRITE);
        return gson.toJson(createJsonObject("scopes", scopeNames));
    }

    public static String createSearchViewExample()
    {
        SearchRequestViewModuleBean bean = SearchRequestViewModuleBean.newSearchRequestViewModuleBean()
                .withName(new I18nProperty("My Search View", "my.search.view"))
                .withKey("my-search-view")
                .withUrl("/search-request.csv")
                .withWeight(100)
                .build();

        return gson.toJson(createModuleArray("jiraSearchRequestViews", bean));
    }

    private static String createDynamicMacroExample()
    {
        DynamicContentMacroModuleBean macroModuleBean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withName(new I18nProperty("Maps", ""))
                .withKey("dynamic-macro-example")
                .withUrl("/render-map?pageTitle={page.title}")
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
                .withEditor(newMacroEditorBean()
                                .withUrl("/map-editor")
                                .withInsertTitle(new I18nProperty("Insert Map", ""))
                                .withEditTitle(new I18nProperty("Edit Map", ""))
                                .build()
                )
                .build();

        return gson.toJson(createModuleArray("dynamicContentMacros", macroModuleBean));
    }

    private static String createStaticMacroExample()
    {
        StaticContentMacroModuleBean macroModuleBean = StaticContentMacroModuleBean.newStaticContentMacroModuleBean()
                .withName(new I18nProperty("Maps", ""))
                .withKey("static-macro-example")
                .withUrl("/render-map?pageTitle={page.title}")
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
                .withEditor(newMacroEditorBean()
                                .withUrl("/map-editor")
                                .withInsertTitle(new I18nProperty("Insert Map", ""))
                                .withEditTitle(new I18nProperty("Edit Map", ""))
                                .build()
                )
                .build();

        return gson.toJson(createModuleArray("staticContentMacros", macroModuleBean));
    }

    private static String createMacroParamsExample()
    {
        MacroParameterBean macroParameterBean = newMacroParameterBean()
                .withIdentifier("view")
                .withName(new I18nProperty("Map View", ""))
                .withDescription(new I18nProperty("Allows switching between view types", ""))
                .withType("enum")
                .withDefaultValue("Map")
                .withMultiple(false)
                .withRequired(true)
                .withValues("Map", "Satellite")
                .build();

        return gson.toJson(createJsonArray("parameters", macroParameterBean));
    }

    private static String createSpaceToolsTabExample()
    {
        SpaceToolsTabModuleBean spaceToolsTabModuleBean = SpaceToolsTabModuleBean.newSpaceToolsTabBean()
                .withName(new I18nProperty("Space Tools Tab", ""))
                .withKey("my-space-tools-tab")
                .withUrl("/space-tools-tab?space_key={space.key}")
                .withLocation("contenttools")
                .build();

        return gson.toJson(createModuleArray("spaceToolsTabs", spaceToolsTabModuleBean));
    }

    private static String createI18nExample()
    {
        I18nProperty bean = new I18nProperty("jim", "my.name.is.jim");
        return gson.toJson(createJsonObject("name", bean));
    }

    private static String createIconExample()
    {
        IconBean bean = newIconBean().withUrl("/my-icon.png").withWidth(16).withHeight(16).build();
        return gson.toJson(createJsonObject("icon", bean));
    }

    private static String createWebitemTargetInlineDialogOptionsExample()
    {
        WebItemTargetBean bean = WebItemTargetBean.newWebItemTargetBean()
                .withType(WebItemTargetType.inlineDialog)
                .withOptions(InlineDialogOptions.newInlineDialogOptions()
                                .withOffsetX("30px")
                                .withOffsetY("20px")
                                .withOnHover(true)
                                .build()
                )
                .build();

        return gson.toJson(createJsonObject("target", bean));
    }

    private static String createWebitemTargetDialogOptionsExample()
    {
        WebItemTargetBean bean = WebItemTargetBean.newWebItemTargetBean()
                .withType(WebItemTargetType.dialog)
                .withOptions(DialogOptions.newDialogOptions()
                                .withHeight("100px")
                                .withWidth("200px")
                                .build()
                )
                .build();

        return gson.toJson(createJsonObject("target", bean));
    }

    private static String createLinkExample()
    {
        LinkBean bean = newLinkBean().withUrl("/go-somewhere").withAltText("somewhere").withTitle("Go Somewhere").build();
        return gson.toJson(createJsonObject("link", bean));
    }

    private static String createSingleConditionExample()
    {
        SingleConditionBean bean = newSingleConditionBean().withCondition("user_is_logged_in").build();
        return gson.toJson(createJsonObject("condition", bean));
    }

    private static String createCompositeConditionExample()
    {
        CompositeConditionBean bean = newCompositeConditionBean()
                .withType(CompositeConditionType.AND)
                .withConditions(
                        newCompositeConditionBean()
                                .withType(CompositeConditionType.OR)
                                .withConditions(
                                        newSingleConditionBean().withCondition(JiraConditions.CAN_ATTACH_FILE_TO_ISSUE).build(),
                                        newSingleConditionBean().withCondition(JiraConditions.IS_ISSUE_ASSIGNED_TO_CURRENT_USER).build()
                                ).build()
                        , newSingleConditionBean().withCondition(JiraConditions.USER_IS_LOGGED_IN).build()
                ).build();

        return gson.toJson(createJsonObject("conditions", bean));
    }

    private static String createUrlExample()
    {
        UrlBean bean = new UrlBean("/my-url");
        return gson.toJson(createJsonObject("endpoint", bean));
    }

    private static String createVendorExample()
    {
        VendorBean bean = newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build();
        return gson.toJson(createJsonObject("vendor", bean));
    }

    private static String createPanelLayoutExample()
    {
        WebPanelLayout bean = new WebPanelLayout("100", "200");
        return gson.toJson(createJsonObject("layout", bean));
    }

    private static String createAuthenticationExample()
    {
        AuthenticationBean bean = newAuthenticationBean().withType(AuthenticationType.JWT).build();
        return gson.toJson(createJsonObject("authentication", bean));
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

    private static String createLinksExample()
    {
        Map<String, String> links = new HashMap<String, String>(2);
        links.put("self", "https://addon.domain.com/atlassian-connect.json");
        links.put("documentation", "https://addon.domain.com/docs");

        JsonObject obj = new JsonObject();
        obj.add("links", gson.toJsonTree(links));

        return gson.toJson(obj);
    }

    private static String createLifecycleExample()
    {
        LifecycleBean bean = LifecycleBean.newLifecycleBean()
                .withInstalled("/installed")
                .withUninstalled("/uninstalled")
                .withEnabled("/enabled")
                .withDisabled("/disabled")
                .build();

        return gson.toJson(bean);
    }

    private static String createMacroEditorExample()
    {
        MacroEditorBean macroEditorBean = newMacroEditorBean()
                .withUrl("/generate_md")
                .withInsertTitle(new I18nProperty("Insert New MarkDown", "macro.md.insert"))
                .withEditTitle(new I18nProperty("Edit MarkDown", "macro.md.edit"))
                .withHeight("300px")
                .withWidth("400px")
                .build();

        return gson.toJson(createJsonObject("editor", macroEditorBean));
    }

    private static String createImagePlaceholderExample()
    {
        ImagePlaceholderBean imagePlaceholderBean = newImagePlaceholderBean()
                .withUrl("/images/placeholder.png")
                .withWidth(100)
                .withHeight(25)
                .withApplyChrome(true)
                .build();

        return gson.toJson(createJsonObject("imagePlaceholder", imagePlaceholderBean));
    }

    private static String createEntityPropertyExample()
    {
        List<EntityPropertyIndexExtractionConfigurationBean> extractionConfiguration = Lists.newArrayList(
                new EntityPropertyIndexExtractionConfigurationBean("attachment.size", EntityPropertyIndexType.number),
                new EntityPropertyIndexExtractionConfigurationBean("attachment.extension", EntityPropertyIndexType.text),
                new EntityPropertyIndexExtractionConfigurationBean("attachment.updated", EntityPropertyIndexType.date)
        );
        EntityPropertyIndexKeyConfigurationBean issueAttachmentIndexConfiguration =
                new EntityPropertyIndexKeyConfigurationBean(extractionConfiguration, "attachment");

        EntityPropertyModuleBean entityPropertyModuleBean = newEntityPropertyModuleBean()
                .withName(new I18nProperty("Attachment Index Document", ""))
                .withEntityType(EntityPropertyType.issue)
                .withKeyConfiguration(issueAttachmentIndexConfiguration)
                .build();

        return gson.toJson(createJsonArray("jiraEntityProperties", entityPropertyModuleBean));
    }

    private static String createEntityPropertyIndexExtractionConfigurationExample()
    {
        EntityPropertyIndexExtractionConfigurationBean bean = new EntityPropertyIndexExtractionConfigurationBean("attachment.size", EntityPropertyIndexType.number);

        return gson.toJson(bean);
    }

    private static String createEntityPropertyIndexKeyConfigurationExample()
    {
        EntityPropertyIndexExtractionConfigurationBean extractionConfiguration =
                new EntityPropertyIndexExtractionConfigurationBean("attachment.size", EntityPropertyIndexType.number);
        EntityPropertyIndexKeyConfigurationBean issueAttachmentIndexConfiguration =
                new EntityPropertyIndexKeyConfigurationBean(Lists.newArrayList(extractionConfiguration), "attachment");

        return gson.toJson(issueAttachmentIndexConfiguration);
    }

    private static String createContentPropertyIndexSchemaExample()
    {
        List<ContentPropertyIndexExtractionConfigurationBean> extractionConfiguration = Lists.newArrayList(
                new ContentPropertyIndexExtractionConfigurationBean("attachment.size", "number"),
                new ContentPropertyIndexExtractionConfigurationBean("attachment.extension", "string"),
                new ContentPropertyIndexExtractionConfigurationBean("attachment.updated", "date"),
                new ContentPropertyIndexExtractionConfigurationBean("attachment.author", "text")
        );
        ContentPropertyIndexKeyConfigurationBean indexConfiguration =
                new ContentPropertyIndexKeyConfigurationBean("attachment", extractionConfiguration);

        ContentPropertyIndexSchemaModuleBean contentPropertyIndexSchemaModuleBean =
                newContentPropertyIndexSchemaModuleBean()
                        .withName(new I18nProperty("Attachment Index Document", ""))
                        .withKeyConfiguration(indexConfiguration)
                        .build();

        return gson.toJson(createJsonArray("contentPropertyIndexSchema", contentPropertyIndexSchemaModuleBean));
    }

    private static JsonObject createJsonArray(String name, ModuleBean bean)
    {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(gson.toJsonTree(bean));
        obj.add(name, arr);
        return obj;
    }

    private static JsonObject createJsonObject(String name, Object bean)
    {
        JsonObject obj = new JsonObject();
        obj.add(name, gson.toJsonTree(bean));
        return obj;
    }

    private static JsonObject createModuleArray(String name, ModuleBean bean)
    {
        JsonObject modules = new JsonObject();
        modules.add("modules", createJsonArray(name, bean));
        return modules;
    }
}
