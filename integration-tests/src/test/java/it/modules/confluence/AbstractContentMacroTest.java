package it.modules.confluence;

import com.atlassian.confluence.it.Page;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.component.editor.toolbars.InsertDropdownMenu;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.Editor;
import com.atlassian.confluence.pageobjects.page.content.EditorPage;
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
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluencePageWithRemoteMacro;
import it.util.TestUser;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractContentMacroTest extends AbstractConfluenceWebDriverTest
{
    public class RichTextBodyMacro
    {
        private String macroKey;
        private String body;

        public RichTextBodyMacro(String macroKey, String body)
        {
            this.macroKey = macroKey;
            this.body = body;
        }

        public String getBody()
        {
            return body;
        }

        public String getMacroKey()
        {
            return macroKey;
        }

        public String getStorageFormat()
        {
            return "<ac:structured-macro ac:name=\"" + macroKey + "\"><ac:rich-text-body>"
                    + StringEscapeUtils.escapeXml(body)
                    + "</ac:rich-text-body></ac:structured-macro>";
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractContentMacroTest.class);

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
    protected static final String SINGLE_PARAM_ID = "param1";
    protected static final String SINGLE_PARAM_NAME = "Parameter 1";

    private static final String ALL_PARAMETER_TYPES_MACRO_NAME = "All Parameters Macro";
    private static final String ALL_PARAMETER_TYPES_MACRO_KEY = "all-parameters-macro";

    private static final String FEATURED_MACRO_NAME = "Featured Macro";
    private static final String FEATURED_MACRO_KEY = "featured-macro";

    private static final String IMAGE_PLACEHOLDER_MACRO_NAME = "Image Placeholder Macro";
    private static final String IMAGE_PLACEHOLDER_MACRO_KEY = "image-placeholder-macro";

    protected static final String EDITOR_MACRO_NAME = "Editor Macro";
    protected static final String EDITOR_MACRO_KEY = "editor-macro";
    protected static final String CUSTOM_TITLE_EDITOR_MACRO_NAME = "Custom Title Macro";
    protected static final String CUSTOM_TITLE_EDITOR_MACRO_KEY = "custom-title-macro";
    private static final String CUSTOM_TITLE = "Custom Title";

    protected static final String HIDDEN_MACRO_NAME = "Hidden Macro";
    protected static final String HIDDEN_MACRO_KEY = "hidden-macro";

    protected ViewPage savedPage;

    protected CreatePage editorPage = null;

    // clean up so that we don't get "org.openqa.selenium.UnhandledAlertException: unexpected alert open" in tests
    @Before
    @After
    public void cleanUpAroundEachTest()
    {
        // dismiss any alerts, because they would stop us from clicking on anything else on the screen
        connectPageOperations.dismissAnyAlerts();
        connectPageOperations.dismissAnyAuiDialog();
        connectPageOperations.dismissClosableAuiMessage();
        connectPageOperations.dismissConfluenceDiscardDraftsPrompt();

        if (null != editorPage)
        {
            try
            {
                editorPage.cancel();
                editorPage = null;
            }
            catch (Throwable t)
            {
                logger.error("Failed to cancel editor page due to the following Throwable. This will most likely result in 'unexpected alert open' exceptions in subsequent tests.", t);
            }
        }

        if (null != savedPage)
        {
            // Don't fail a test because cleanup failed
            try
            {
                rpc.removePage(savedPage.getPageId());
                savedPage = null;
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
            }
        }
    }

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
                .withKey(LONG_BODY_MACRO_KEY)
                .withName(new I18nProperty(LONG_BODY_MACRO_NAME, ""))
                .withBodyType(MacroBodyType.PLAIN_TEXT)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createShortBodyMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL + "?body={macro.body}")
                .withKey(SHORT_BODY_MACRO_KEY)
                .withName(new I18nProperty(SHORT_BODY_MACRO_NAME, ""))
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createParameterMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL + "?param1={param1}")
                .withKey(PARAMETER_MACRO_KEY)
                .withName(new I18nProperty(PARAMETER_MACRO_NAME, ""))
                .withParameters(newMacroParameterBean()
                                .withIdentifier(SINGLE_PARAM_ID)
                                .withName(new I18nProperty(SINGLE_PARAM_NAME, ""))
                                .withType("string")
                                .build()
                )
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createAllParametersMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL)
                .withKey(ALL_PARAMETER_TYPES_MACRO_KEY)
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

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createCustomEditorTitleMacro(T builder)
    {
        return builder
                .withKey(CUSTOM_TITLE_EDITOR_MACRO_KEY)
                .withUrl("/echo/params?footy={footy}")
                .withName(new I18nProperty(CUSTOM_TITLE_EDITOR_MACRO_NAME, ""))
                .withEditor(MacroEditorBean.newMacroEditorBean()
                                .withInsertTitle(new I18nProperty(CUSTOM_TITLE, ""))
                                .withUrl("/render-editor")
                                .withHeight("200px")
                                .withWidth("300px")
                                .build()
                )
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createEditorMacro(T builder)
    {
        return builder
                .withKey(EDITOR_MACRO_KEY)
                .withUrl("/echo/params?footy={footy}")
                .withName(new I18nProperty(EDITOR_MACRO_NAME, ""))
                .withEditor(MacroEditorBean.newMacroEditorBean()
                                .withUrl("/render-editor")
                                .withHeight("200px")
                                .withWidth("300px")
                                .build()
                )
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createHiddenMacro(T builder)
    {
        return builder
                .withKey(HIDDEN_MACRO_KEY)
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(HIDDEN_MACRO_NAME, ""))
                .withHidden(true)
                .build();
    }

    @BeforeClass
    public static void overridePageObjects()
    {
        product.getPageBinder().override(MacroBrowserDialog.class, ConfluenceMacroBrowserDialog.class);
        product.getPageBinder().override(EditorContent.class, ConfluenceEditorContent.class);
        product.getPageBinder().override(InsertDropdownMenu.class, ConfluenceInsertMenu.class);
    }

    @Test
    public void testMacroIsListed() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, SIMPLE_MACRO_NAME);

        try
        {
            assertThat(macroBrowserAndEditor.macroForm, is(not(nullValue())));
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancel();
        }
    }

    @Test
    public void testParameterTypes() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, ALL_PARAMETER_TYPES_MACRO_NAME);

        try
        {
            MacroForm macroForm = macroBrowserAndEditor.macroForm;
            assertThat(macroForm.hasField("attachment").byDefaultTimeout(), is(true));
            assertThat(macroForm.hasField("boolean").byDefaultTimeout(), is(true));
            assertThat(macroForm.hasField("content").byDefaultTimeout(), is(true));
            assertThat(macroForm.hasField("enum").byDefaultTimeout(), is(true));
            assertThat(macroForm.hasField("spacekey").byDefaultTimeout(), is(true));
            assertThat(macroForm.hasField("string").byDefaultTimeout(), is(true));
            assertThat(macroForm.hasField("username").byDefaultTimeout(), is(true));
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancel();
        }
    }

    @Test
    public void testParameterLabel() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Parameter Page"));
        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, PARAMETER_MACRO_NAME);

        try
        {
            assertTrue(macroBrowserAndEditor.macroForm.getField(SINGLE_PARAM_ID).isVisible());

            WebElement label = connectPageOperations.findLabel("macro-param-" + SINGLE_PARAM_ID);
            assertThat(label.getText(), is(SINGLE_PARAM_NAME));
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancel();
        }
    }

    @Test
    public void testFeaturedMacro() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        final Editor editor = editorPage.getEditor();
        enableMacrosDropdown(editor);
        ConfluenceInsertMenu insertMenu = (ConfluenceInsertMenu) editor.openInsertMenu();
        assertThat(insertMenu.hasEntryWithKey(FEATURED_MACRO_KEY), is(true));
    }

    @Test
    public void testImagePlaceholder() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Image Placeholder Macro"));
        selectMacroAndSave(editorPage, IMAGE_PLACEHOLDER_MACRO_NAME);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getEditor().getContent();
        String url = editorContent.getImagePlaceholderUrl();

        assertThat(url, is(getAddonBaseUrl() + "/images/placeholder.png"));
    }

    @Test
    public void testMacroEditorShowsAddOnContent() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);

        selectMacro(editorPage, EDITOR_MACRO_NAME, new Runnable()
        {
            @Override
            public void run()
            {
                RemotePluginDialog dialog = null;

                try
                {
                    dialog = connectPageOperations.findDialog(EDITOR_MACRO_KEY);
                    String content = dialog.getValueById("description");
                    assertThat(content, is("Select from:"));
                }
                finally
                {
                    if (dialog != null)
                    {
                        dialog.cancel();
                    }
                }
            }
        });
    }

    @Test
    public void testMacroEditorCancels() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        selectMacro(editorPage, EDITOR_MACRO_NAME, macroDialogCanceller(EDITOR_MACRO_KEY));
    }

    @Test
    public void testMacroEditorCustomTitle() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);

        selectMacro(editorPage, CUSTOM_TITLE_EDITOR_MACRO_NAME, new Runnable()
        {
            @Override
            public void run()
            {
                RemotePluginDialog dialog = null;

                try
                {
                    dialog = connectPageOperations.findDialog(CUSTOM_TITLE_EDITOR_MACRO_KEY);
                    assertThat(dialog.getTitle(), is(CUSTOM_TITLE));
                }
                finally
                {
                    if (dialog != null)
                    {
                        dialog.cancel();
                    }
                }
            }
        });
    }

    @Test
    public void testMacroEditorDefaultTitle() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);

        selectMacro(editorPage, EDITOR_MACRO_NAME, new Runnable()
        {
            @Override
            public void run()
            {
                RemotePluginDialog dialog = null;

                try
                {
                    dialog = connectPageOperations.findDialog(EDITOR_MACRO_KEY);
                    assertThat(dialog.getTitle(), containsString(EDITOR_MACRO_NAME));
                }
                finally
                {
                    if (dialog != null)
                    {
                        dialog.cancel();
                    }
                }
            }
        });
    }

    @Test
    public void testMacroEditorSubmits() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        selectMacro(editorPage, EDITOR_MACRO_NAME, macroDialogSubmitter(EDITOR_MACRO_KEY));
    }

    @Test
    public void testHiddenMacro() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, HIDDEN_MACRO_NAME);

        try
        {
            assertThat(macroBrowserAndEditor.macroForm, is(nullValue()));
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancel();
        }
    }

    protected void addSimpleMacroToComment() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);

        try
        {
            editorPage.setTitle(randomName("The macro is in the comment!"));
            savedPage = save(editorPage);
            editorPage = null;

            confluenceOps.addComment(some(TestUser.ADMIN), String.valueOf(savedPage.getPageId()), pageWithMacro(SIMPLE_MACRO_KEY));
            product.visit(ConfluencePageWithRemoteMacro.class, savedPage.getTitle(), SIMPLE_MACRO_KEY);
        }
        catch (Exception e)
        {
            try
            {
                if (null != editorPage)
                {
                    editorPage.cancel();
                }
            }
            catch (Throwable t)
            {
                // don't care
            }

            throw e;
        }
    }

    protected ViewPage save(EditorPage editorPage)
    {
        return editorPage.saveWithKeyboardShortcut();
    }

    private String pageWithMacro(String macroName)
    {
        return format("<div class=\"%1$s\"><ac:macro ac:name=\"%1$s\" /></div>", macroName);
    }

    protected ViewPage getMacroContent(User user, String macroName, String title) throws Exception
    {
        // create the page with the macro
        editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);

        try
        {
            editorPage.dismissEditorNotifications();
            editorPage.setTitle(randomName(title));
            selectMacroAndSave(editorPage, macroName);
            savedPage = save(editorPage);
            editorPage = null;
            final long pageId = savedPage.getPageId(); // need to get it here because it parses the page, so if we log out it throws!

            // view the page as the specified user
            if (TestUser.ADMIN.confUser().equals(user))
            {
                return savedPage;
            }
            else
            {
                getProduct().logOutFast();

                if (null == user)
                {
                    return getProduct().viewPage(String.valueOf(pageId));
                }
                else
                {
                    return getProduct().loginAndView(user, new Page(pageId));
                }
            }
        }
        catch (Exception e)
        {
            try
            {
                if (null != editorPage)
                {
                    editorPage.cancel();
                }
            }
            catch (Throwable t)
            {
                // don't care
            }

            throw e;
        }
    }

    protected abstract String getAddonBaseUrl();
}
