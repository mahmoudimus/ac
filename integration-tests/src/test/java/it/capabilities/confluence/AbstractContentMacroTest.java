package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.component.editor.InsertMenu;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceInsertMenu;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroBrowserDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroForm;
import com.atlassian.plugin.connect.test.pageobjects.confluence.MacroList;
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
    protected static final String SIMPLE_MACRO_NAME = "Simple Macro";
    protected static final String SIMPLE_MACRO_KEY = "simple-macro";
    private static final String SIMPLE_MACRO_ALIAS = "unlikelytocollide";

    private static final String ALL_PARAMETER_TYPES_MACRO_NAME = "All Parameters Macro";

    private static final String FEATURED_MACRO_NAME = "Featured Macro";
    private static final String FEATURED_MACRO_KEY = "featured-macro";

    private static final String IMAGE_PLACEHOLDER_MACRO_NAME = "Image Placeholder Macro";
    private static final String IMAGE_PLACEHOLDER_MACRO_KEY = "image-placeholder-macro";

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createImagePlaceholderMacro(T builder)
    {
        return builder
                .withKey(IMAGE_PLACEHOLDER_MACRO_KEY)
                .withUrl("/render-macro")
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
                .withUrl("/render-macro")
                .withName(new I18nProperty(FEATURED_MACRO_NAME, ""))
                .withIcon(newIconBean()
                        .withUrl("images/macro-icon.png")
                        .build()
                )
                .withFeatured(true)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createAllParametersMacro(T builder)
    {
        return builder
                .withUrl("/render-macro")
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
                .withUrl("/render-macro")
                .withName(new I18nProperty(SIMPLE_MACRO_NAME, ""))
                .withIcon(newIconBean()
                        .withUrl("images/macro-icon.png")
                        .build()
                )
                .withAliases(SIMPLE_MACRO_ALIAS)
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

    @Test
    public void testMacroIsListed() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(SIMPLE_MACRO_NAME);

        assertThat(macro, is(not(nullValue())));
    }

    //@Test -- will only work once we're on 5.3-OD-15
    public void testAlias() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();

        MacroList macroList = editorContent.autocompleteMacro(SIMPLE_MACRO_ALIAS);
        assertThat(macroList.hasEntryWithKey(SIMPLE_MACRO_KEY), is(true));
    }

    @Test
    public void testParameterTypes() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
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
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        ConfluenceInsertMenu insertMenu = (ConfluenceInsertMenu) editorPage.openInsertMenu();

        assertThat(insertMenu.hasEntryWithKey(FEATURED_MACRO_KEY), is(true));
    }

    //@Test -- will only work in 5.3-OD-13 and later
    public void testImagePlaceholder() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Image Placeholder Macro");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        macroBrowser.selectAndInsertMacro(IMAGE_PLACEHOLDER_MACRO_KEY);

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        String url = editorContent.getImagePlaceholderUrl();

        editorPage.cancel();

        assertThat(url, is(getAddonBaseUrl() + "/images/placeholder.png"));
    }

    protected abstract String getAddonBaseUrl();

}
