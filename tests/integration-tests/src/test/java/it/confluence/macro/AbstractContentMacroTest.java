package it.confluence.macro;

import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.Editor;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceInsertMenu;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

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

public abstract class AbstractContentMacroTest extends ConfluenceWebDriverTestBase
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

    @BeforeClass
    public static void setUpClass()
    {
        getProduct().logOutFast();
    }

    @After
    public void logoutAfter()
    {
        getProduct().logOutFast();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createImagePlaceholderMacro(T builder)
    {
        return builder
                .withKey(IMAGE_PLACEHOLDER_MACRO_KEY)
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(IMAGE_PLACEHOLDER_MACRO_NAME, null))
                .withImagePlaceholder(ImagePlaceholderBean.newImagePlaceholderBean()
                                .withUrl("/images/placeholder.png")
                                .withWidth(50)
                                .withHeight(50)
                                .withApplyChrome(true)
                                .build()
                )
                .withParameters(newMacroParameterBean()
                        .withIdentifier("param1")
                        .withName(new I18nProperty("Param", null))
                        .withType("string")
                        .build())
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createFeaturedMacro(T builder)
    {
        return builder
                .withKey(FEATURED_MACRO_KEY)
                .withUrl(DEFAULT_MACRO_URL)
                .withName(new I18nProperty(FEATURED_MACRO_NAME, null))
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
                .withName(new I18nProperty(LONG_BODY_MACRO_NAME, null))
                .withBodyType(MacroBodyType.PLAIN_TEXT)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createShortBodyMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL + "?body={macro.body}")
                .withKey(SHORT_BODY_MACRO_KEY)
                .withName(new I18nProperty(SHORT_BODY_MACRO_NAME, null))
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createParameterMacro(T builder)
    {
        return builder
                .withUrl(DEFAULT_MACRO_URL + "?param1={param1}")
                .withKey(PARAMETER_MACRO_KEY)
                .withName(new I18nProperty(PARAMETER_MACRO_NAME, null))
                .withParameters(newMacroParameterBean()
                                .withIdentifier(SINGLE_PARAM_ID)
                                .withName(new I18nProperty(SINGLE_PARAM_NAME, null))
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
                .withName(new I18nProperty(ALL_PARAMETER_TYPES_MACRO_NAME, null))
                .withParameters(
                        newMacroParameterBean()
                                .withIdentifier("attachment")
                                .withName(new I18nProperty("Attachment", null))
                                .withType("attachment")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("boolean")
                                .withName(new I18nProperty("Boolean", null))
                                .withType("boolean")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("content")
                                .withName(new I18nProperty("Confluence Content", null))
                                .withType("confluence-content")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("enum")
                                .withName(new I18nProperty("Enum", null))
                                .withType("enum")
                                .withValues("val1", "val2")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("spacekey")
                                .withName(new I18nProperty("Space Key", null))
                                .withType("spacekey")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("string")
                                .withName(new I18nProperty("String", null))
                                .withType("string")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("username")
                                .withName(new I18nProperty("User Name", null))
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
                .withName(new I18nProperty(SIMPLE_MACRO_NAME, null))
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
                .withName(new I18nProperty(CUSTOM_TITLE_EDITOR_MACRO_NAME, null))
                .withEditor(MacroEditorBean.newMacroEditorBean()
                                .withInsertTitle(new I18nProperty(CUSTOM_TITLE, null))
                                .withUrl("/render-editor")
                                .withHeight("200px")
                                .withWidth("300px")
                                .build()
                )
                .build();
    }

    public static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createEditorMacro(T builder)
    {
        return builder
                .withKey(EDITOR_MACRO_KEY)
                .withUrl("/echo/params?footy={footy}")
                .withName(new I18nProperty(EDITOR_MACRO_NAME, null))
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
                .withName(new I18nProperty(HIDDEN_MACRO_NAME, null))
                .withHidden(true)
                .build();
    }

    @Test
    public void testMacroIsListed() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, SIMPLE_MACRO_NAME);

        try
        {
            assertThat(macroBrowserAndEditor.macroForm, is(not(nullValue())));
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancelAndWaitUntilClosed();
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testParameterTypes() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
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
            macroBrowserAndEditor.browserDialog.clickCancelAndWaitUntilClosed();
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testParameterLabel() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
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
            macroBrowserAndEditor.browserDialog.clickCancelAndWaitUntilClosed();
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testFeaturedMacro() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        final Editor editor = editorPage.getEditor();
        enableMacrosDropdown(editorPage);
        ConfluenceInsertMenu insertMenu = (ConfluenceInsertMenu) editor.openInsertMenu();
        try
        {
            assertThat(insertMenu.hasEntryWithKey(FEATURED_MACRO_KEY), is(true));
        }
        finally
        {
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testImagePlaceholder() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Image Placeholder Macro"));
        selectMacroAndSave(editorPage, IMAGE_PLACEHOLDER_MACRO_NAME);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getEditor().getContent();
        String url = editorContent.getImagePlaceholderUrl();
        try
        {
            assertThat(url, is(getAddonBaseUrl() + "/images/placeholder.png"));
        }
        finally
        {
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testMacroEditorShowsAddOnContent() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);

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
                        dialog.cancelAndWaitUntilHidden();
                    }
                }
            }
        });
        cancelEditor(editorPage);
    }

    @Test
    public void testMacroEditorCancels() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        selectMacro(editorPage, EDITOR_MACRO_NAME, macroDialogCanceller(EDITOR_MACRO_KEY));
        cancelEditor(editorPage);
    }

    @Test
    public void testMacroEditorCustomTitle() throws Exception
    {
        final CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);

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
                        dialog.cancelAndWaitUntilHidden();
                    }
                    cancelEditor(editorPage);
                }
            }
        });
    }

    @Test
    public void testMacroEditorDefaultTitle() throws Exception
    {
        final CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);

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
                        dialog.cancelAndWaitUntilHidden();
                    }
                    cancelEditor(editorPage);
                }
            }
        });
    }

    @Test
    public void testMacroEditorSubmits() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        try
        {
            selectMacro(editorPage, EDITOR_MACRO_NAME, macroDialogSubmitter(EDITOR_MACRO_KEY));
        }
        finally
        {
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testHiddenMacro() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, HIDDEN_MACRO_NAME);

        try
        {
            assertThat(macroBrowserAndEditor.macroForm, is(nullValue()));
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancelAndWaitUntilClosed();
            cancelEditor(editorPage);
        }
    }

    protected abstract String getAddonBaseUrl();

    protected void addCommentWithMacro(String pageId) throws MalformedURLException, XmlRpcFault
    {
        String body = format("<div class=\"%1$s\"><ac:macro ac:name=\"%1$s\" /></div>", SIMPLE_MACRO_KEY);
        confluenceOps.addComment(some(testUserFactory.basicUser()), pageId, body);
    }
}
