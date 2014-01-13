package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.component.editor.InsertMenu;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceInsertMenu;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroBrowserDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroForm;
import com.atlassian.plugin.connect.test.pageobjects.confluence.MacroList;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class TestDynamicContentMacro extends AbstractConfluenceWebDriverTest
{
    private static final String SIMPLE_MACRO_NAME = "Simple Macro";
    private static final String SIMPLE_MACRO_ALIAS = "unlikelytocollide";

    private static final String ALL_PARAMETER_TYPES_MACRO_NAME = "All Parameters Macro";
    private static final String LONG_BODY_MACRO_NAME = "Long Body Macro";
    private static final String SHORT_BODY_MACRO_NAME = "Short Body Macro";
    private static final String FEATURED_MACRO_NAME = "Featured Macro";
    private static final String IMAGE_PLACEHOLDER_MACRO_NAME = "Image Placeholder Macro";
    private static final String PARAMETER_MACRO_NAME = "SingleParam Macro";

    private static ConnectRunner remotePlugin;

    private static DynamicContentMacroModuleBean simpleMacro;
    private static DynamicContentMacroModuleBean allParameterTypesMacro;
    private static DynamicContentMacroModuleBean longBodyMacro;
    private static DynamicContentMacroModuleBean shortBodyMacro;
    private static DynamicContentMacroModuleBean featuredMacro;
    private static DynamicContentMacroModuleBean imagePlaceholderMacro;
    private static DynamicContentMacroModuleBean parameterMacro;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        simpleMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro")
                .withName(new I18nProperty(SIMPLE_MACRO_NAME, ""))
                .withIcon(newIconBean()
                        .withUrl("images/macro-icon.png")
                        .build()
                )
                .withAliases(SIMPLE_MACRO_ALIAS)
                .build();

        allParameterTypesMacro = newDynamicContentMacroModuleBean()
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

        longBodyMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro?hash={macro.hash}")
                .withName(new I18nProperty(LONG_BODY_MACRO_NAME, ""))
                .withBodyType(MacroBodyType.PLAIN_TEXT)
                .build();

        shortBodyMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro?body={macro.body}")
                .withName(new I18nProperty(SHORT_BODY_MACRO_NAME, ""))
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        featuredMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro")
                .withName(new I18nProperty(FEATURED_MACRO_NAME, ""))
                .withIcon(newIconBean()
                        .withUrl("images/macro-icon.png")
                        .build()
                )
                .withFeatured(true)
                .build();

        imagePlaceholderMacro = newDynamicContentMacroModuleBean()
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

        parameterMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro")
                .withName(new I18nProperty(PARAMETER_MACRO_NAME, ""))
                .withParameters(newMacroParameterBean()
                        .withIdentifier("param1")
                        .withName(new I18nProperty("Param 1", ""))
                        .withType("string")
                        .build()
                )
                .build();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapabilities("dynamicContentMacros",
                        simpleMacro,
                        allParameterTypesMacro,
                        longBodyMacro,
                        shortBodyMacro,
                        featuredMacro,
                        imagePlaceholderMacro,
                        parameterMacro
                )
                .addRoute("/render-macro", ConnectAppServlets.helloWorldServlet())
                .addRoute("/images/placeholder.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .start();

        overridePageObjects();
    }

    private static void overridePageObjects()
    {
        product.getPageBinder().override(MacroBrowserDialog.class, ConfluenceMacroBrowserDialog.class);
        product.getPageBinder().override(EditorContent.class, ConfluenceEditorContent.class);
        product.getPageBinder().override(MacroForm.class, ConfluenceMacroForm.class);
        product.getPageBinder().override(InsertMenu.class, ConfluenceInsertMenu.class);
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testMacroIsListed() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(SIMPLE_MACRO_NAME);

        assertThat(macro, is(not(nullValue())));
    }

    @Test
    public void testMacroIsRendered() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Simple Macro on Page");

        selectSimpleMacro(editorPage);

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(simpleMacro.getKey(), 0);
        String content = renderedMacro.getIFrameElementText("hello-world-message");
        rpc.removePage(savedPage.getPageId());

        assertThat(content, is("Hello world"));
    }

    //@Test -- will only work once we're on 5.3-OD-15
    public void testAlias() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();

        MacroList macroList = editorContent.autocompleteMacro(SIMPLE_MACRO_ALIAS);
        assertThat(macroList.hasEntryWithKey(simpleMacro.getKey()), is(true));
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

        assertThat(insertMenu.hasEntryWithKey(featuredMacro.getKey()), is(true));
    }

    @Test
    public void testBodyInclusion() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Short Body Macro");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(SHORT_BODY_MACRO_NAME);
        macro.select();

        macroBrowser.insertMacro();
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        editorContent.setMacroBody("a short body", true);

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(shortBodyMacro.getKey(), 0);
        String body = renderedMacro.getFromQueryString("body");
        rpc.removePage(savedPage.getPageId());

        assertThat(body, is("<p>a short body</p>"));
    }

    @Test
    public void testBodyHashInclusion() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Long Body Macro");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(LONG_BODY_MACRO_NAME);
        macro.select();
        macroBrowser.insertMacro();

        String body = StringUtils.repeat("x ", 200);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        editorContent.setMacroBody(body, false);

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(longBodyMacro.getKey(), 0);
        String hash = renderedMacro.getFromQueryString("hash");
        rpc.removePage(savedPage.getPageId());

        assertThat(hash, is(DigestUtils.md5Hex(body)));
    }

    @Test
    public void testParameterInclusion() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Parameter Page");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(PARAMETER_MACRO_NAME);
        ConfluenceMacroForm macroForm = (ConfluenceMacroForm) macro.select();

        macroForm.getAutocompleteField("param1").setValue("param value");
        macroBrowser.insertMacro();

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(parameterMacro.getKey(), 0);
        String value = renderedMacro.getFromQueryString("param1");

        rpc.removePage(savedPage.getPageId());

        assertThat(value, is("param value"));
    }

    @Test
    public void testMultipleMacrosOnPage() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Multiple Macros");

        selectSimpleMacro(editorPage);
        selectSimpleMacro(editorPage);

        ViewPage savedPage = editorPage.save();

        RenderedMacro renderedMacro1 = connectPageOperations.findMacro(simpleMacro.getKey(), 0);
        String content1 = renderedMacro1.getIFrameElementText("hello-world-message");

        RenderedMacro renderedMacro2 = connectPageOperations.findMacro(simpleMacro.getKey(), 1);
        String content2 = renderedMacro2.getIFrameElementText("hello-world-message");

        rpc.removePage(savedPage.getPageId());

        assertThat(content1, is(content2));
    }

    //@Test -- will only work in 5.3-OD-13 and later
    public void testImagePlaceholder() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Image Placeholder Macro");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(IMAGE_PLACEHOLDER_MACRO_NAME);
        macro.select();
        macroBrowser.insertMacro();

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        String url = editorContent.getImagePlaceholderUrl();

        editorPage.cancel();

        assertThat(url, is(remotePlugin.getAddon().getBaseUrl() + "/images/placeholder.png"));
    }

    private void selectSimpleMacro(CreatePage editorPage)
    {
        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(SIMPLE_MACRO_NAME);
        macro.select();
        macroBrowser.insertMacro();
    }
}
