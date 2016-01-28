package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonEventDataBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.ContentPropertyIndexExtractionConfigurationBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexFieldType;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean;
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
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;
import com.atlassian.plugin.connect.modules.beans.nested.MatcherBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.UISupportValueType;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.InlineDialogOptions;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData.newConnectAddonEventData;
import static com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean.newContentPropertyModuleBean;
import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean.newEntityPropertyModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean.newImagePlaceholderBean;
import static com.atlassian.plugin.connect.modules.beans.nested.LinkBean.newLinkBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean.newMacroEditorBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions.newDialogOptions;
import static java.util.Arrays.asList;

@SuppressWarnings("UnusedDeclaration")
public class ConnectJsonExamples
{
    private static final Gson gson = ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create();

    public static final String ADDON_EXAMPLE = createAddonExample();
    public static final String AUTHENTICATION_EXAMPLE = createAuthenticationExample();
    public static final String AUTOCONVERT_EXAMPLE = createAutoconvertExample();
    public static final String AUTOCONVERT_MATCHER_EXAMPLE = createMatcherExample();
    public static final String COMPOSITE_CONDITION_EXAMPLE = createCompositeConditionExample();
    public static final String DIALOG_EXAMPLE = createDialogExample();
    public static final String DYNAMIC_MACRO_EXAMPLE = createDynamicMacroExample();
    public static final String ENTITY_PROPERTY_EXAMPLE = createEntityPropertyExample();
    public static final String ENTITY_PROPERTY_INDEX_EXTRACTION_CONFIGURATION_EXAMPLE = createEntityPropertyIndexExtractionConfigurationExample();
    public static final String ENTITY_PROPERTY_INDEX_KEY_CONFIGURATION_EXAMPLE = createEntityPropertyIndexKeyConfigurationExample();
    public static final String GLOBAL_PERMISSION_EXAMPLE = createGlobalPermissionExample();
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
    public static final String DASHBOARD_ITEM_EXAMPLE = createDashboardItemExample();
    public static final String PRJ_ADMIN_PAGE_EXAMPLE = createProjectAdminPageExample();
    public static final String PROJECT_PERMISSION_EXAMPLE = createProjectPermissionExample();
    public static final String REPORT_EXAMPLE = createReportExample();
    public static final String SCOPES_EXAMPLE = createScopesExample();
    public static final String SEARCH_VIEW_EXAMPLE = createSearchViewExample();
    public static final String SINGLE_CONDITION_EXAMPLE = createSingleConditionExample();
    public static final String SPACE_TOOLS_TAB_EXAMPLE = createSpaceToolsTabExample();
    public static final String STATIC_MACRO_EXAMPLE = createStaticMacroExample();
    public static final String TAB_PANEL_EXAMPLE = createTabPanelExample();
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
    public static final String CONTENT_PROPERTY_EXAMPLE = createContentPropertyExample();
    public static final String CONTENT_PROPERTY_UI_SUPPORT = createAttachmentTypeUISupportExample();
    public static final String CONTENT_PROPERTY_INDEX_EXTRACTION_CONFIGURATION_EXAMPLE = createContentPropertyIndexExtractionConfigurationExample();
    public static final String CONTENT_PROPERTY_INDEX_KEY_CONFIGURATION_EXAMPLE = createContentPropertyIndexKeyConfigurationExample();
    public static final String MACRO_RENDER_MODES_EXAMPLE = createDynamicMacroExampleForRenderModes();

    public static final String LIFECYCLE_PAYLOAD_EXAMPLE = createLifecyclePayloadExample();

    public static final String RENDER_MODE_EXAMPLE_WORD = createRenderModesExampleWord();
    public static final String RENDER_MODE_EXAMPLE_PDF = createRenderModesExamplePdf();
    public static final String RENDER_MODE_EXAMPLE_HTML_EXPORT = createRenderModesExampleHtmlExport();
    public static final String RENDER_MODE_EXAMPLE_EMAIL = createRenderModesExampleEmail();
    public static final String RENDER_MODE_EXAMPLE_FEED = createRenderModesExampleFeed();
    public static final String RENDER_MODE_EXAMPLE_DEFAULT = createRenderModesExampleDefault();

    public static final String EMBEDDED_STATIC_MACRO_EXAMPLE = MACRO_RENDER_MODES_EXAMPLE;
    public static final String ATTACHMENT_SIZE_ALIAS = "attachmentSize";

    private static String createRenderModesExampleWord()
    {
        return gson.toJson(MacroRenderModesBean
                .newMacroRenderModesBean()
                .withWord(createEmbeddedStaticMacroBean("/render-map-word"))
                .build());
    }

    private static String createRenderModesExamplePdf()
    {
        return gson.toJson(MacroRenderModesBean
                .newMacroRenderModesBean()
                .withPdf(createEmbeddedStaticMacroBean("/render-map-pdf"))
                .build());
    }

    private static String createRenderModesExampleHtmlExport()
    {
        return gson.toJson(MacroRenderModesBean
                .newMacroRenderModesBean()
                .withHtmlExport(createEmbeddedStaticMacroBean("/render-map-html-export"))
                .build());
    }

    private static String createRenderModesExampleEmail()
    {
        return gson.toJson(MacroRenderModesBean
                .newMacroRenderModesBean()
                .withEmail(createEmbeddedStaticMacroBean("/render-map-email"))
                .build());
    }

    private static String createRenderModesExampleFeed()
    {
        return gson.toJson(MacroRenderModesBean
                .newMacroRenderModesBean()
                .withFeed(createEmbeddedStaticMacroBean("/render-map-rss-feed"))
                .build());
    }

    private static String createRenderModesExampleDefault()
    {
        return gson.toJson(MacroRenderModesBean
                .newMacroRenderModesBean()
                .withDefaultfallback(createEmbeddedStaticMacroBean("/render-map-default"))
                .build());
    }

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
                .build();

        return gson.toJson(addonBean);
    }

    private static I18nProperty i18nProperty(String name)
    {
        return new I18nProperty(name, null);
    }

    private static EmbeddedStaticContentMacroBean createEmbeddedStaticMacroBeanStatic()
    {
        return EmbeddedStaticContentMacroBean.newEmbeddedStaticContentMacroModuleBean()
                .withUrl("/render-map-static")
                .build();
    }

    private static EmbeddedStaticContentMacroBean createEmbeddedStaticMacroBean(String url)
    {
        return EmbeddedStaticContentMacroBean.newEmbeddedStaticContentMacroModuleBean()
                .withUrl(url)
                .build();
    }

    private static String createPageExample()
    {
        JsonElement generalPageModuleBean = createJsonArrayWithSingleObject(ConnectPageModuleBean.newPageBean()
                .withName(i18nProperty("My General Page"))
                .withKey("my-general-page")
                .withUrl("/my-general-page")
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(80).withWidth(80).build())
                .build());
        JsonElement adminPageModuleBean = createJsonArrayWithSingleObject(ConnectPageModuleBean.newPageBean()
                .withName(i18nProperty("My Admin Page"))
                .withKey("my-admin-page")
                .withUrl("/my-admin-page")
                .build());
        JsonElement configurePageModuleBean = gson.toJsonTree(ConnectPageModuleBean.newPageBean()
                .withName(i18nProperty("My Configure Page"))
                .withKey("my-config-page")
                .withUrl("/my-config-page")
                .build());
        JsonElement postInstallPageModuleBean = gson.toJsonTree(ConnectPageModuleBean.newPageBean()
                .withName(new I18nProperty("My Post-Install Page", "mypostinstallpage.name"))
                .withKey("my-post-install-page")
                .withUrl("/my-post-install-page")
                .build());
        JsonElement userProfilePageModuleBean = createJsonArrayWithSingleObject(ConnectPageModuleBean.newPageBean()
                .withName(i18nProperty("My Confluence User Profile Page"))
                .withKey("my-confluence-user-profile-page")
                .withUrl("/my-confluence-user-profile-page")
                .build());

        
        JsonObject object = createModuleArray(ImmutableMap.of(
                "generalPages", generalPageModuleBean,
                "adminPages", adminPageModuleBean,
                "configurePage", configurePageModuleBean,
                "postInstallPage", postInstallPageModuleBean,
                "profilePages", userProfilePageModuleBean
        ));
        return gson.toJson(object);
    }

    private static String createProjectAdminPageExample()
    {
        ConnectProjectAdminTabPanelModuleBean pageModuleBean = ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean()
                .withName(i18nProperty("Admin Panel"))
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
                .withName(i18nProperty("My Web Item"))
                .withUrl("/my-web-item")
                .withKey("web-item-example")
                .withLocation("system.preset.filters")
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(16).withWidth(16).build())
                .withStyleClasses("webitem", "system-present-webitem")
                .withTooltip(i18nProperty("Example tooltip"))
                .withWeight(200)
                .build();

        return gson.toJson(createModuleArray("webItems", webItemModuleBean));
    }

    private static String createTabPanelExample()
    {
        JsonElement issueTabPanelBean = createJsonArrayWithSingleObject(ConnectTabPanelModuleBean.newTabPanelBean()
                .withName(i18nProperty("My Issue Tab Panel"))
                .withKey("my-issue-tab")
                .withUrl("/my-issue-tab")
                .withWeight(100)
                .build());
        JsonElement projectTabPanelBean = createJsonArrayWithSingleObject(ConnectTabPanelModuleBean.newTabPanelBean()
                .withName(i18nProperty("My Project Tab Panel"))
                .withKey("my-project-tab")
                .withUrl("/my-project-tab")
                .withWeight(100)
                .build());
        JsonElement userProfileTabPanelBean = createJsonArrayWithSingleObject(ConnectTabPanelModuleBean.newTabPanelBean()
                .withName(i18nProperty("My Profile Tab Panel"))
                .withKey("my-profile-tab")
                .withUrl("/my-profile-tab")
                .withWeight(100)
                .build());
        JsonObject object = createModuleArray(ImmutableMap.of(
                "jiraIssueTabPanels", issueTabPanelBean,
                "jiraProjectTabPanels", projectTabPanelBean,
                "jiraProfileTabPanels", userProfileTabPanelBean
        ));
        return gson.toJson(object);
    }

    private static String createReportExample()
    {
        ReportModuleBean reportModuleBean = ReportModuleBean.newBuilder()
                .withKey("report-example")
                .withUrl("/report?projectKey={project.key}")
                .withName(i18nProperty("Example Report"))
                .withDescription(i18nProperty("This is an example report"))
                .withReportCategory(ReportCategory.AGILE)
                .withWeight(100)
                .withThumbnailUrl("http://example.com/images/report-example-thumbnail.png")
                .build();
        return gson.toJson(createModuleArray("jiraReports", reportModuleBean));
    }

    private static String createPostFunctionExample()
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withName(i18nProperty("My Function"))
                .withKey("workflow-example")
                .withDescription(i18nProperty("My Description"))
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
                .withName(i18nProperty("My Web Panel"))
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
                .withName(i18nProperty("My Web Section"))
                .withKey("my-web-section")
                .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                .withWeight(50)
                .build();

        return gson.toJson(createModuleArray("webSections", webSectionModuleBean));
    }

    private static String createGlobalPermissionExample()
    {
        GlobalPermissionModuleBean globalPermissionModuleBean = GlobalPermissionModuleBean.newGlobalPermissionModuleBean()
                .withKey("my-global-permission")
                .withName(i18nProperty("My Global Permission"))
                .withDescription(i18nProperty("Custom global permission for my add-on"))
                .withAnonymousAllowed(false)
                .build();

        return gson.toJson(createModuleArray("jiraGlobalPermissions", globalPermissionModuleBean));
    }

    private static String createProjectPermissionExample()
    {
        ProjectPermissionModuleBean projectPermissionModuleBean = ProjectPermissionModuleBean.newProjectPermissionModuleBean()
                .withKey("my-project-permission")
                .withName(i18nProperty("My Project Permission"))
                .withDescription(i18nProperty("Custom project permission for attachments"))
                .withCategory(ProjectPermissionCategory.ATTACHMENTS)
                .build();

        return gson.toJson(createModuleArray("jiraProjectPermissions", projectPermissionModuleBean));
    }

    private static String createBlueprintExample()
    {
        BlueprintModuleBean blueprintModuleBean = BlueprintModuleBean.newBlueprintModuleBean()
                .withName(i18nProperty("Simple Remote Blueprint"))
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

    private static String createScopesExample()
    {
        HashSet<ScopeName> scopeNames = Sets.newHashSet(ScopeName.READ, ScopeName.WRITE);
        return gson.toJson(createJsonObject("scopes", scopeNames));
    }

    public static String createSearchViewExample()
    {
        SearchRequestViewModuleBean bean = SearchRequestViewModuleBean.newSearchRequestViewModuleBean()
                .withName(i18nProperty("My Search View"))
                .withKey("my-search-view")
                .withUrl("/search-request.csv")
                .withWeight(100)
                .build();

        return gson.toJson(createModuleArray("jiraSearchRequestViews", bean));
    }

    private static String createDialogExample() {
        DialogModuleBean bean = DialogModuleBean.newDialogBean()
                .withKey("dialog-example")
                .withUrl("/my-dialog-content")
                .withOptions(newDialogOptions().withSize("fullscreen").build())
                .build();

        return gson.toJson(createModuleArray("dialogs", bean));
    }

    private static String createDynamicMacroExample()
    {
        DynamicContentMacroModuleBean macroModuleBean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withName(i18nProperty("Maps"))
                .withKey("dynamic-macro-example")
                .withUrl("/render-map?pageTitle={page.title}")
                .withAliases("map")
                .withBodyType(MacroBodyType.NONE)
                .withOutputType(MacroOutputType.BLOCK)
                .withCategories("visuals")
                .withDescription(i18nProperty("Shows a configurable map"))
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
                                .withName(i18nProperty("Map View"))
                                .withDescription(i18nProperty("Allows switching between view types"))
                                .withType("enum")
                                .withDefaultValue("Map")
                                .withMultiple(false)
                                .withRequired(true)
                                .withValues("Map", "Satellite")
                                .build()
                )
                .withEditor(newMacroEditorBean()
                                .withUrl("/map-editor")
                                .withInsertTitle(i18nProperty("Insert Map"))
                                .withEditTitle(i18nProperty("Edit Map"))
                                .build()
                )
                .withRenderModes(MacroRenderModesBean
                        .newMacroRenderModesBean()
                        .withPdf(createEmbeddedStaticMacroBean("/render-map-pdf"))
                        .withDefaultfallback(createEmbeddedStaticMacroBeanStatic())
                        .build())
                .withAutoconvert(AutoconvertBean.newAutoconvertBean()
                        .withUrlParameter("url")
                        .withMatchers(MatcherBean.newMatcherBean()
                                        .withPattern("https://www.example.com/maps/{}/{}")
                                        .build(),
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://www.example.com/map-editor/{}")
                                        .build())
                        .build())
                .build();

        return gson.toJson(createModuleArray("dynamicContentMacros", macroModuleBean));
    }

    private static String createDynamicMacroExampleForRenderModes()
    {
        DynamicContentMacroModuleBean macroModuleBean = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withName(i18nProperty("Maps"))
                .withKey("dynamic-macro-example")
                .withUrl("/render-map?pageTitle={page.title}")
                .withRenderModes(MacroRenderModesBean
                        .newMacroRenderModesBean()
                        .withPdf(createEmbeddedStaticMacroBean("/render-map-pdf"))
                        .withDefaultfallback(createEmbeddedStaticMacroBeanStatic())
                        .build())
                .build();

        return gson.toJson(createModuleArray("dynamicContentMacros", macroModuleBean));
    }

    private static String createStaticMacroExample()
    {
        StaticContentMacroModuleBean macroModuleBean = StaticContentMacroModuleBean.newStaticContentMacroModuleBean()
                .withName(i18nProperty("Maps"))
                .withKey("static-macro-example")
                .withUrl("/render-map?pageTitle={page.title}")
                .withAliases("map")
                .withBodyType(MacroBodyType.NONE)
                .withOutputType(MacroOutputType.BLOCK)
                .withCategories("visuals")
                .withDescription(i18nProperty("Shows a configurable map"))
                .withDocumentation(newLinkBean()
                                .withUrl("http://docs.example.com/addons/maps")
                                .build()
                )
                .withFeatured(true)
                .withIcon(newIconBean().withUrl("/maps/icon.png").withHeight(80).withWidth(80).build())
                .withParameters(newMacroParameterBean()
                                .withIdentifier("view")
                                .withName(i18nProperty("Map View"))
                                .withDescription(i18nProperty("Allows switching between view types"))
                                .withType("enum")
                                .withDefaultValue("Map")
                                .withMultiple(false)
                                .withRequired(true)
                                .withValues("Map", "Satellite")
                                .build()
                )
                .withEditor(newMacroEditorBean()
                                .withUrl("/map-editor")
                                .withInsertTitle(i18nProperty("Insert Map"))
                                .withEditTitle(i18nProperty("Edit Map"))
                                .build()
                ).withAutoconvert(AutoconvertBean.newAutoconvertBean()
                        .withUrlParameter("url")
                        .withMatchers(MatcherBean.newMatcherBean()
                                        .withPattern("https://www.example.com/maps/{}/{}")
                                        .build(),
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://www.example.com/map-editor/{}")
                                        .build())
                        .build())
                .build();

        return gson.toJson(createModuleArray("staticContentMacros", macroModuleBean));
    }

    private static String createMacroParamsExample()
    {
        MacroParameterBean macroParameterBean = newMacroParameterBean()
                .withIdentifier("view")
                .withName(i18nProperty("Map View"))
                .withDescription(i18nProperty("Allows switching between view types"))
                .withType("enum")
                .withDefaultValue("Map")
                .withMultiple(false)
                .withRequired(true)
                .withValues("Map", "Satellite")
                .build();

        return gson.toJson(createJsonObjectContainingArray("parameters", macroParameterBean));
    }

    private static String createSpaceToolsTabExample()
    {
        SpaceToolsTabModuleBean spaceToolsTabModuleBean = SpaceToolsTabModuleBean.newSpaceToolsTabBean()
                .withName(i18nProperty("Space Tools Tab"))
                .withKey("my-space-tools-tab")
                .withUrl("/space-tools-tab?space_key={space.key}")
                .withLocation("contenttools")
                .build();

        return gson.toJson(createModuleArray("spaceToolsTabs", spaceToolsTabModuleBean));
    }

    private static String createI18nExample()
    {
        I18nProperty bean = new I18nProperty("My Label", null);
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
                .withOptions(newDialogOptions()
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
        return gson.toJson(bean);
    }

    private static String createCompositeConditionExample()
    {
        List<ConditionalBean> conditions = asList(
            newCompositeConditionBean()
                .withType(CompositeConditionType.OR)
                .withConditions(
                        newSingleConditionBean().withCondition("can_attach_file_to_issue").build(),
                        newSingleConditionBean().withCondition("is_issue_assigned_to_current_user").build()
                ).build(),
            newSingleConditionBean().withCondition("user_is_logged_in").build()
        );

        return gson.toJson(createJsonObject("conditions", conditions));
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

    private static String createLifecyclePayloadExample()
    {
        ConnectAddonEventDataBuilder dataBuilder = newConnectAddonEventData();

        dataBuilder.withBaseUrl("http://example.atlassian.net")
                .withPluginKey("installed-addon-key")
                .withClientKey("unique-client-identifier")
                .withPublicKey("MIGf....ZRWzwIDAQAB")
                .withSharedSecret("a-secret-key-not-to-be-lost")
                .withPluginsVersion("version-of-connect")
                .withServerVersion("server-version")
                .withServiceEntitlementNumber("SEN-number")
                .withProductType("jira")
                .withDescription("Atlassian JIRA at https://example.atlassian.net")
                .withEventType("installed");

        ConnectAddonEventData data = dataBuilder.build();

        return gson.toJson(data);
    }

    private static String createMacroEditorExample()
    {
        MacroEditorBean macroEditorBean = newMacroEditorBean()
                .withUrl("/generate_md")
                .withInsertTitle(i18nProperty("Insert New MarkDown"))
                .withEditTitle(i18nProperty("Edit MarkDown"))
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
                new EntityPropertyIndexExtractionConfigurationBean("attachment.size", EntityPropertyIndexType.number, ATTACHMENT_SIZE_ALIAS),
                new EntityPropertyIndexExtractionConfigurationBean("attachment.extension", EntityPropertyIndexType.text, "attachmentExtension"),
                new EntityPropertyIndexExtractionConfigurationBean("attachment.updated", EntityPropertyIndexType.date, "attachmentUpdatedDate")
        );
        EntityPropertyIndexKeyConfigurationBean issueAttachmentIndexConfiguration =
                new EntityPropertyIndexKeyConfigurationBean(extractionConfiguration, "attachment");

        EntityPropertyModuleBean entityPropertyModuleBean = newEntityPropertyModuleBean()
                .withKey("attachment-entity-property")
                .withName(i18nProperty("Attachment Index Document"))
                .withEntityType(EntityPropertyType.issue)
                .withKeyConfiguration(issueAttachmentIndexConfiguration)
                .build();

        return gson.toJson(createJsonObjectContainingArray("jiraEntityProperties", entityPropertyModuleBean));
    }

    private static String createEntityPropertyIndexExtractionConfigurationExample()
    {
        EntityPropertyIndexExtractionConfigurationBean bean = new EntityPropertyIndexExtractionConfigurationBean("attachment.size", EntityPropertyIndexType.number, ATTACHMENT_SIZE_ALIAS);

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

    private static String createAutoconvertExample()
    {
        DynamicContentMacroModuleBean dynamicMacroWithAutoconvert = newDynamicContentMacroModuleBean()
                .withUrl("/dynamic-macro?url={url}")
                .withKey("dynamic-macro-with-autoconvert")
                .withName(new I18nProperty("Dynamic Macro With Autoconvert", null))
                .withParameters(
                        newMacroParameterBean()
                                .withIdentifier("url")
                                .withName(i18nProperty("URL"))
                                .withType("string")
                                .build())
                .withAutoconvert(AutoconvertBean.newAutoconvertBean()
                        .withUrlParameter("url")
                        .withMatchers(MatcherBean.newMatcherBean()
                                        .withPattern("https://www.facebook.com/{}/about")
                                        .build(),
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://www.facebook.com/{}/music")
                                        .build(),
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://www.facebook.com/{}/movies/{}")
                                        .build())
                        .build())
                .build();

        return gson.toJson(dynamicMacroWithAutoconvert);
    }

    private static String createMatcherExample()
    {

        MatcherBean matcher = MatcherBean.newMatcherBean()
                .withPattern("https://www.facebook.com/{}/about")
                .build();

        return gson.toJson(matcher);
    }

    private static String createContentPropertyIndexExtractionConfigurationExample()
    {
        return gson.toJson(createAttachmentTypeContentPropertyExtraction());
    }

    private static String createContentPropertyIndexKeyConfigurationExample()
    {
        List<ContentPropertyIndexExtractionConfigurationBean> extractionConfiguration = getContentPropertyIndexExtractionConfigurationBeans();
        return gson.toJson(new ContentPropertyIndexKeyConfigurationBean("attachment", extractionConfiguration));
    }

    private static List<ContentPropertyIndexExtractionConfigurationBean> getContentPropertyIndexExtractionConfigurationBeans()
    {
        return Lists.newArrayList(
                createContentPropertyIndexExtractionConfigurationBean("attachment.size", ContentPropertyIndexFieldType.number),
                createAttachmentTypeContentPropertyExtraction(),
                createContentPropertyIndexExtractionConfigurationBean("attachment.updated", ContentPropertyIndexFieldType.date)
        );
    }

    private static ContentPropertyIndexExtractionConfigurationBean createAttachmentTypeContentPropertyExtraction()
    {
        UISupportModuleBean uiSupport = createAttachmentTypeUISupportBean();

        return createContentPropertyIndexExtractionConfigurationBean("attachment.type", ContentPropertyIndexFieldType.string, "contentType", uiSupport);
    }

    private static String createAttachmentTypeUISupportExample()
    {
        return gson.toJson(createAttachmentTypeUISupportBean());
    }

    private static UISupportModuleBean createAttachmentTypeUISupportBean()
    {
        return UISupportModuleBean.newUISupportModuleBean()
                .withName(new I18nProperty("Content Type", "attachment.type.name"))
                .withDataUri("/data/content-types")
                .withDefaultOperator("~")
                .withTooltip(new I18nProperty("Content Type Tooltip", "attachment.type.tooltip"))
                .withValueType(UISupportValueType.STRING)
                .build();
    }

    public static ContentPropertyModuleBean createContentPropertyExampleBean()
    {
        List<ContentPropertyIndexExtractionConfigurationBean> extractionConfiguration = getContentPropertyIndexExtractionConfigurationBeans();

        ContentPropertyIndexKeyConfigurationBean indexConfiguration =
                new ContentPropertyIndexKeyConfigurationBean("attachment", extractionConfiguration);

        return newContentPropertyModuleBean()
                .withName(i18nProperty("Attachment Index Document"))
                .withKeyConfiguration(indexConfiguration)
                .build();
    }

    private static String createContentPropertyExample()
    {
        return gson.toJson(createJsonObjectContainingArray("confluenceContentProperties", createContentPropertyExampleBean()));
    }

    private static String createDashboardItemExample()
    {
        final DashboardItemModuleBean dashboardItemExample = DashboardItemModuleBean.newBuilder()
                .withDescription(i18nProperty("Dashboard item description"))
                .withName(i18nProperty("Dashboard item title"))
                .withThumbnailUrl("atlassian-icon-16.png")
                .withUrl("/dashboard-item-test?dashboardItemId={dashboardItem.id}&dashboardId={dashboard.id}&view={dashboardItem.viewType}")
                .withKey("dashboard-item-key")
                .configurable(true)
                .build();

        return gson.toJson(createJsonObjectContainingArray("jiraDashboardItems", dashboardItemExample));
    }

    private static JsonObject createJsonObjectContainingArray(String name, ModuleBean bean)
    {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(gson.toJsonTree(bean));
        obj.add(name, arr);
        return obj;
    }
    
    private static JsonArray createJsonArrayWithSingleObject(Object bean)
    {
        JsonArray arr = new JsonArray();
        arr.add(gson.toJsonTree(bean));
        return arr;
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
        modules.add("modules", createJsonObjectContainingArray(name, bean));
        return modules;
    }

    private static JsonObject createModuleArray(ImmutableMap<String, ? extends JsonElement> modules)
    {
        JsonObject modulesObject = new JsonObject();
        for (Map.Entry<String, ? extends JsonElement> module : modules.entrySet())
        {
            modulesObject.add(module.getKey(), module.getValue());
        }
        JsonObject obj = new JsonObject();
        obj.add("modules", modulesObject);
        return obj;
    }

    private static ContentPropertyIndexExtractionConfigurationBean createContentPropertyIndexExtractionConfigurationBean(String objectName, ContentPropertyIndexFieldType type)
    {
        return createContentPropertyIndexExtractionConfigurationBean(objectName, type, null, null);
    }

    private static ContentPropertyIndexExtractionConfigurationBean createContentPropertyIndexExtractionConfigurationBean(String objectName,
                                                                                                                         ContentPropertyIndexFieldType type,
                                                                                                                         String alias, UISupportModuleBean uiSupport)
    {
        ContentPropertyIndexExtractionConfigurationBeanBuilder builder = new ContentPropertyIndexExtractionConfigurationBeanBuilder()
                .withObjectName(objectName)
                .withType(type);
        if (alias != null)
        {
            builder = builder.withAlias(alias);
        }
        if (uiSupport != null)
        {
            builder = builder.withUiSupport(uiSupport);
        }
        return builder.build();
    }
}
