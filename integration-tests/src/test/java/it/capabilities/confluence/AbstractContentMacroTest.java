package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.component.editor.InsertMenu;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceInsertMenu;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroBrowserDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroForm;
import com.atlassian.plugin.connect.test.pageobjects.confluence.MacroList;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public abstract class AbstractContentMacroTest extends AbstractConfluenceWebDriverTest
{
    protected static final String DEFAULT_MACRO_URL = "/render-macro";

    protected static final String SIMPLE_MACRO_NAME = "Simple Macro";
    protected static final String SIMPLE_MACRO_KEY = "simple-macro";
    private static final String SIMPLE_MACRO_ALIAS = "unlikelytocollide";

    protected static final String LONG_BODY_MACRO_NAME = "Long Body Macro";
    protected static final String LONG_BODY_MACRO_KEY = "long-body-macro";

    protected static final String SHORT_BODY_MACRO_NAME = "Short Body Macro";
    protected static final String SHORT_BODY_MACRO_KEY = "short-body-macro";

    protected static final String PARAMETER_MACRO_NAME = "Single Param Macro";
    protected static final String PARAMETER_MACRO_KEY = "single-param-macro";

    private static final String ALL_PARAMETER_TYPES_MACRO_NAME = "All Parameters Macro";

    private static final String FEATURED_MACRO_NAME = "Featured Macro";
    private static final String FEATURED_MACRO_KEY = "featured-macro";

    private static final String IMAGE_PLACEHOLDER_MACRO_NAME = "Image Placeholder Macro";
    private static final String IMAGE_PLACEHOLDER_MACRO_KEY = "image-placeholder-macro";

    private static final String EDITOR_MACRO_NAME = "Editor Macro";
    private static final String EDITOR_MACRO_KEY = "editor-macro";

    protected ViewPage savedPage;

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createImagePlaceholderMacro(T builder)
    {
        return builder
                .withKey(IMAGE_PLACEHOLDER_MACRO_KEY)
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(IMAGE_PLACEHOLDER_MACRO_NAME, ""))
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                        .withUrl("/images/placeholder.png")
                        .withWidth(50)
                        .withHeight(50)
                        .withApplyChrome(true)
                        .build()
                )
                .withParameters(newMacroParameterBean()
                        .withIdentifier("param1")
                        .withName(new I18nProperty("Param", ""))
                        .withType("string")
                        .build())
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createFeaturedMacro(T builder)
    {
        return builder
                .withKey(FEATURED_MACRO_KEY)
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(FEATURED_MACRO_NAME, ""))
                .withIcon(newIconBean()
                        .withUrl("images/macro-icon.png")
                        .build()
                )
                .withFeatured(true)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createLongBodyMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL + "?hash={macro.hash}")
                .withName(new I18nProperty(LONG_BODY_MACRO_NAME, ""))
                .withBodyType(MacroBodyType.PLAIN_TEXT)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createShortBodyMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL + "?body={macro.body}")
                .withName(new I18nProperty(SHORT_BODY_MACRO_NAME, ""))
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createParameterMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(PARAMETER_MACRO_NAME, ""))
                .withParameters(newMacroParameterBean()
                        .withIdentifier("param1")
                        .withName(new I18nProperty("Param 1", ""))
                        .withType("string")
                        .build()
                )
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createAllParametersMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(ALL_PARAMETER_TYPES_MACRO_NAME, ""))
                .withParameters(
                        newMacroParameterBean()
                                .withIdentifier("attachment")
                                .withName(new I18nProperty("Attachment", ""))
                                .withType("attachment")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("boolean")
                                .withName(new I18nProperty("Boolean", ""))
                                .withType("boolean")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("content")
                                .withName(new I18nProperty("Confluence Content", ""))
                                .withType("confluence-content")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("enum")
                                .withName(new I18nProperty("Enum", ""))
                                .withType("enum")
                                .withValues("val1", "val2")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("spacekey")
                                .withName(new I18nProperty("Space Key", ""))
                                .withType("spacekey")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("string")
                                .withName(new I18nProperty("String", ""))
                                .withType("string")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("username")
                                .withName(new I18nProperty("User Name", ""))
                                .withType("username")
                                .build()
                )
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createSimpleMacro(T builder)
    {
        return builder
                .withKey(SIMPLE_MACRO_KEY)
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(SIMPLE_MACRO_NAME, ""))
                .withIcon(newIconBean()
                        .withUrl("images/macro-icon.png")
                        .build()
                )
                .withAliases(SIMPLE_MACRO_ALIAS)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createEditorMacro(T builder)
    {
        return builder
                .withKey(EDITOR_MACRO_KEY)
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(EDITOR_MACRO_NAME, ""))
                .withEditor(MacroEditorBean.newMacroEditorBean()
                        .withEditTitle(new I18nProperty("Edit Title", ""))
                        .withInsertTitle(new I18nProperty("Insert Title", ""))
                        .withUrl("/render-editor")
                        .withHeight("200px")
                        .withWidth("300px")
                        .build()
                )
                .build();
    }

    @BeforeClass
    public static void overridePageObjects()
    {
        product.getPageBinder().override(MacroBrowserDialog.class, ConfluenceMacroBrowserDialog.class);
        product.getPageBinder().override(EditorContent.class, ConfluenceEditorContent.class);
        product.getPageBinder().override(MacroForm.class, ConfluenceMacroForm.class);
        product.getPageBinder().override(InsertMenu.class, ConfluenceInsertMenu.class);
    }

    @After
    public void cleanup()
    {
        if (null != savedPage)
        {
            rpc.removePage(savedPage.getPageId());
        }
    }

    @Test
    public void testMacroIsListed() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(SIMPLE_MACRO_NAME);

        assertThat(macro, is(not(nullValue())));
    }

    @Test
    public void testAlias() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();

        MacroList macroList = editorContent.autoCompleteMacroList(SIMPLE_MACRO_ALIAS);
        assertThat(macroList.hasEntryWithKey(SIMPLE_MACRO_KEY), is(true));
    }

    @Test
    public void testParameterTypes() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);

        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(ALL_PARAMETER_TYPES_MACRO_NAME);
        ConfluenceMacroForm macroForm = (ConfluenceMacroForm) macro.select();

        List<String> parameterNames = macroForm.getParameterNames();
        assertThat(parameterNames, containsInAnyOrder(
                "attachment",
                "boolean",
                "content",
                "enum",
                "spacekey",
                "string",
                "username"
        ));
    }

    @Test
    public void testFeaturedMacro() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        ConfluenceInsertMenu insertMenu = (ConfluenceInsertMenu) editorPage.openInsertMenu();

        assertThat(insertMenu.hasEntryWithKey(FEATURED_MACRO_KEY), is(true));
    }

    @Test
    public void testImagePlaceholder() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Image Placeholder Macro");

        selectMacro(editorPage, IMAGE_PLACEHOLDER_MACRO_NAME);

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        String url = editorContent.getImagePlaceholderUrl();

        editorPage.cancel();

        assertThat(url, is(getAddonBaseUrl() + "/images/placeholder.png"));
    }

    @Test
    public void testMacroEditor() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);

        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(EDITOR_MACRO_NAME);
        macro.select();

        RemotePluginDialog dialog = connectPageOperations.findDialog(EDITOR_MACRO_KEY);

        String content = dialog.getEmbeddedPage().getValueById("hello-world-message");

        assertThat(content, is("Hello world"));
    }


    protected abstract String getAddonBaseUrl();

    protected void selectMacro(CreatePage editorPage, String macroName)
    {
        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(macroName);
        macro.select();
        macroBrowser.clickSave();
    }
}
