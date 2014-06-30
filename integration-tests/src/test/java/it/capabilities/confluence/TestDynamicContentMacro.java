package it.capabilities.confluence;

import com.atlassian.confluence.it.Page;
import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.EditContentPage;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;

public class TestDynamicContentMacro extends AbstractContentMacroTest
{
    private static final String SMALL_INLINE_MACRO_NAME = "Small Inline Macro";
    private static final String SMALL_INLINE_MACRO_KEY = "small-inline-macro";

    private static final String CLIENT_SIDE_BODY_MACRO_NAME = "Client Side Body Editing";
    private static final String CLIENT_SIDE_BODY_MACRO_KEY = "client-side-body-editing";
    private static final String EDITED_MACRO_BODY = "cat pictures and more";

    private static final String CLIENT_SIDE_BODY_MACRO_SCRIPT_NAME = "Client Side Body Editing Script Injection Attempt";
    private static final String CLIENT_SIDE_BODY_MACRO_SCRIPT_KEY = "script-injection-attempt";
    private static final String EDITED_MACRO_BODY_SCRIPT = "<strong>must</strong> be removed:<script>alert('bad, bad, bad')</\"+\"script>";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        DynamicContentMacroModuleBean simpleMacro = createSimpleMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean allParameterTypesMacro = createAllParametersMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean featuredMacro = createFeaturedMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean imagePlaceholderMacro = createImagePlaceholderMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean longBodyMacro = createLongBodyMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean shortBodyMacro = createShortBodyMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean parameterMacro = createParameterMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean editorMacro = createEditorMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean customTitleEditorMacro = createCustomEditorTitleMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean hiddenMacro = createHiddenMacro(newDynamicContentMacroModuleBean());

        DynamicContentMacroModuleBean smallInlineMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-no-resize-macro")
                .withKey(SMALL_INLINE_MACRO_KEY)
                .withName(new I18nProperty(SMALL_INLINE_MACRO_NAME, ""))
                .withOutputType(MacroOutputType.INLINE)
                .withWidth("60px")
                .withHeight("30px")
                .build();

        DynamicContentMacroModuleBean clientSideBodyEditingMacro = newDynamicContentMacroModuleBean()
                .withUrl("/echo/params?body={macro.body}")
                .withKey(CLIENT_SIDE_BODY_MACRO_KEY)
                .withName(new I18nProperty(CLIENT_SIDE_BODY_MACRO_NAME, ""))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .withEditor(MacroEditorBean.newMacroEditorBean()
                        .withUrl("/macro-body-editor")
                        .withHeight("200px")
                        .withWidth("300px")
                        .build())
                .build();

        DynamicContentMacroModuleBean clientSideBodyEditingMacroScriptInjection = newDynamicContentMacroModuleBean()
                .withUrl("/echo/params?body={macro.body}")
                .withKey(CLIENT_SIDE_BODY_MACRO_SCRIPT_KEY)
                .withName(new I18nProperty(CLIENT_SIDE_BODY_MACRO_SCRIPT_NAME, ""))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .withEditor(MacroEditorBean.newMacroEditorBean()
                        .withUrl("/macro-body-editor-script")
                        .withHeight("200px")
                        .withWidth("300px")
                        .build())
                .build();


        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), RemotePluginUtils.randomPluginKey())
                .setAuthenticationToNone()
                .addModules("dynamicContentMacros",
                        simpleMacro,
                        allParameterTypesMacro,
                        longBodyMacro,
                        shortBodyMacro,
                        featuredMacro,
                        imagePlaceholderMacro,
                        parameterMacro,
                        smallInlineMacro,
                        editorMacro,
                        customTitleEditorMacro,
                        hiddenMacro,
                        clientSideBodyEditingMacro,
                        clientSideBodyEditingMacroScriptInjection
                )
                .addRoute(DEFAULT_MACRO_URL, ConnectAppServlets.helloWorldServlet())
                .addRoute("/render-editor", ConnectAppServlets.macroEditor())
                .addRoute("/macro-body-editor", ConnectAppServlets.macroBodyEditor(EDITED_MACRO_BODY))
                .addRoute("/macro-body-editor-script", ConnectAppServlets.macroBodyEditor(EDITED_MACRO_BODY_SCRIPT))
                .addRoute("/echo/params", ConnectAppServlets.echoQueryParametersServlet())
                .addRoute("/render-no-resize-macro", ConnectAppServlets.noResizeServlet())
                .addRoute("/images/placeholder.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .addRoute("/images/macro-icon.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .start();
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
    public void testMacroIsRendered() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle(unique("Simple Macro on Page"));

        selectMacro(editorPage, SIMPLE_MACRO_NAME);

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(SIMPLE_MACRO_KEY, 0);
        String content = renderedMacro.getIFrameElementText("hello-world-message");

        assertThat(content, is("Hello world"));
    }

    @Test
    public void testBodyInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle(unique("Short Body Macro"));

        selectMacro(editorPage, SHORT_BODY_MACRO_NAME);

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getEditor().getContent();
        editorContent.setRichTextMacroBody("a short body");

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(SHORT_BODY_MACRO_KEY, 0);
        String body = renderedMacro.getFromQueryString("body");

        assertThat(body, is("<p>a short body</p>"));
    }

    @Test
    public void testBodyHashInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle(unique("Long Body Macro"));

        selectMacro(editorPage, LONG_BODY_MACRO_NAME);

        String body = StringUtils.repeat("x ", 200);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getEditor().getContent();
        editorContent.setPlainTextMacroBody(body);

        savedPage = editorPage.save();

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(LONG_BODY_MACRO_KEY, 0);
        String hash = renderedMacro.getFromQueryString("hash");

        assertThat(hash, is(DigestUtils.md5Hex(body)));
    }

    @Test
    public void testParameterInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle(unique("Parameter Page"));

        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(PARAMETER_MACRO_NAME);
        MacroForm macroForm = macro.select();

        macroForm.getAutocompleteField("param1").setValue("param value");
        macroBrowser.clickSave();

        savedPage = editorPage.save();

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(PARAMETER_MACRO_KEY);
        String value = renderedMacro.getFromQueryString("param1");

        assertThat(value, is("param value"));
    }

    @Test
    public void testMultipleMacrosOnPage() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle(unique("Multiple Macros"));

        selectMacro(editorPage, SIMPLE_MACRO_NAME);
        selectMacro(editorPage, SIMPLE_MACRO_NAME);

        savedPage = editorPage.save();

        connectPageOperations.waitUntilNConnectIFramesPresent(2); // preempt flakiness

        RenderedMacro renderedMacro1 = connectPageOperations.findMacroWithIdPrefix(SIMPLE_MACRO_KEY, 0);
        String content1 = renderedMacro1.getIFrameElementText("hello-world-message");

        RenderedMacro renderedMacro2 = connectPageOperations.findMacroWithIdPrefix(SIMPLE_MACRO_KEY, 1);
        String content2 = renderedMacro2.getIFrameElementText("hello-world-message");

        assertThat(content1, is("Hello world"));
        assertThat(content2, is("Hello world"));
    }

    @Test
    public void testMacroDimensions() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle(unique("Small Inline Macro"));

        selectMacro(editorPage, SMALL_INLINE_MACRO_NAME);

        savedPage = editorPage.save();

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(SMALL_INLINE_MACRO_KEY);

        assertThat(renderedMacro.getIFrameSize(), both(hasProperty("width", is(60))).and(hasProperty("height", is(30))));
    }

    @Test
    public void testMacroEditorSavesParameters() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle(unique("Macro Editor"));

        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(EDITOR_MACRO_NAME);
        macro.select();

        RemotePluginDialog dialog = connectPageOperations.findDialog(EDITOR_MACRO_KEY);
        dialog.submit();

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(EDITOR_MACRO_KEY);
        String content = renderedMacro.getIFrameElementText("footy");

        assertThat(content, is("footy: American Football"));
    }

    @Test
    public void testMacroEditorCanReadBody() throws Exception
    {
        RichTextBodyMacro macro = new RichTextBodyMacro(CLIENT_SIDE_BODY_MACRO_KEY, "cat pictures go here");
        createAndEditPage(CLIENT_SIDE_BODY_MACRO_NAME, "<p>" + macro.getStorageFormat() + "</p>");

        RemotePluginDialog dialog = connectPageOperations.editMacro(macro.getMacroKey());
        try
        {
            String content = dialog.getValueById("macro-body");
            assertThat(content, is(macro.getBody()));
        }
        finally
        {
            dialog.cancel();
        }
    }

    @Test
    public void testMacroEditorCanWriteBody() throws Exception
    {
        RichTextBodyMacro macro = new RichTextBodyMacro(CLIENT_SIDE_BODY_MACRO_KEY, "");
        EditContentPage editorPage = createAndEditPage(CLIENT_SIDE_BODY_MACRO_NAME, "<p>" + macro.getStorageFormat() + "</p>");

        RemotePluginDialog dialog = connectPageOperations.editMacro(macro.getMacroKey());
        dialog.submit();

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(CLIENT_SIDE_BODY_MACRO_KEY);
        String content = renderedMacro.getIFrameElementText("body");

        assertThat(content, is("body: " + EDITED_MACRO_BODY));
    }

    @Test
    public void testBodyIsSanitized() throws Exception
    {
        RichTextBodyMacro macro = new RichTextBodyMacro(CLIENT_SIDE_BODY_MACRO_SCRIPT_KEY, "");
        EditContentPage editorPage = createAndEditPage(CLIENT_SIDE_BODY_MACRO_SCRIPT_NAME, "<p>" + macro.getStorageFormat() + "</p>");

        RemotePluginDialog dialog = connectPageOperations.editMacro(macro.getMacroKey());
        dialog.submit();

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(CLIENT_SIDE_BODY_MACRO_SCRIPT_KEY);
        String content = renderedMacro.getIFrameElementText("body");

        assertThat(content, is("body: <strong>must</strong> be removed:"));
    }

    @Override
    protected String getAddonBaseUrl()
    {
        return remotePlugin.getAddon().getBaseUrl();
    }

    private EditContentPage createAndEditPage(String pageName, String pageContent) throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(ConfluenceOps.ConfluenceUser.ADMIN),
                TestSpace.DEMO.getKey(), pageName, pageContent);
        return product.visit(EditContentPage.class, new Page(Long.parseLong(pageData.getId())));
    }
}
