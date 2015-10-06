package it.confluence.macro;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.it.Page;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.EditContentPage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.client.WebDriverSessionAwareDownloader;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluencePageWithRemoteMacro;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import it.confluence.MacroStorageFormatBuilder;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;
import static com.atlassian.plugin.connect.test.Utils.loadResourceAsString;
import static it.matcher.ParamMatchers.isVersionNumber;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;

public class TestDynamicContentMacro extends AbstractContentMacroTest
{
    private static final String SMALL_INLINE_MACRO_NAME = "Small Inline Macro";
    private static final String SMALL_INLINE_MACRO_KEY = "small-inline-macro";
    private static final String SMALL_INLINE_MACRO_DESCRIPTION = "small-inline-macro-description";

    private static final String CLIENT_SIDE_BODY_MACRO_NAME = "Client Side Body Editing";
    private static final String CLIENT_SIDE_BODY_MACRO_KEY = "client-side-body-editing";
    private static final String CLIENT_SIDE_BODY_MACRO_DESCRIPTION = "<script>alert(1);</script>";
    private static final String EDITED_MACRO_BODY = "cat pictures and more";

    private static final String CLIENT_SIDE_BODY_MACRO_SCRIPT_NAME = "Client Side Body Editing Script Injection Attempt";
    private static final String CLIENT_SIDE_BODY_MACRO_SCRIPT_KEY = "script-injection-attempt";
    private static final String EDITED_MACRO_BODY_SCRIPT = "<strong>must</strong> be removed:<script>alert('bad, bad, bad')</\"+\"script>";

    private static final String TABLE_MACRO_NAME = "Table Macro";
    private static final String TABLE_MACRO_KEY = "table-macro";

    private static final String SLOW_MACRO_NAME = "Slow Macro";
    private static final String SLOW_MACRO_KEY = "slow-macro";

    private static final String DYNAMIC_MACRO_KEY = "dynamic-macro";
    private static final String DYNAMIC_MACRO_NAME = "Dynamic Macro";


    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
        String addonKey = AddonTestUtils.randomAddOnKey();

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
                .withDescription(new I18nProperty(SMALL_INLINE_MACRO_DESCRIPTION, null))
                .withKey(SMALL_INLINE_MACRO_KEY)
                .withName(new I18nProperty(SMALL_INLINE_MACRO_NAME, null))
                .withOutputType(MacroOutputType.INLINE)
                .withWidth("60px")
                .withHeight("30px")
                .build();

        DynamicContentMacroModuleBean clientSideBodyEditingMacro = newDynamicContentMacroModuleBean()
                .withUrl("/echo/params?body={macro.body}")
                .withDescription(new I18nProperty(CLIENT_SIDE_BODY_MACRO_DESCRIPTION, null))
                .withKey(CLIENT_SIDE_BODY_MACRO_KEY)
                .withName(new I18nProperty(CLIENT_SIDE_BODY_MACRO_NAME, null))
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
                .withName(new I18nProperty(CLIENT_SIDE_BODY_MACRO_SCRIPT_NAME, null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .withEditor(MacroEditorBean.newMacroEditorBean()
                        .withUrl("/macro-body-editor-script")
                        .withHeight("200px")
                        .withWidth("300px")
                        .build())
                .build();

        DynamicContentMacroModuleBean macroInTableMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro-in-table-macro")
                .withKey(TABLE_MACRO_KEY)
                .withName(new I18nProperty(TABLE_MACRO_NAME, null))
                .withOutputType(MacroOutputType.BLOCK)
                .build();

        DynamicContentMacroModuleBean slowMacro = newDynamicContentMacroModuleBean()
                .withUrl("/slow-macro")
                .withKey(SLOW_MACRO_KEY)
                .withName(new I18nProperty(SLOW_MACRO_NAME, null))
                .build();

        DynamicContentMacroModuleBean dynamicMacroWithFallback = newDynamicContentMacroModuleBean()
                .withUrl("/dynamic-macro")
                .withKey(DYNAMIC_MACRO_KEY)
                .withName(new I18nProperty(DYNAMIC_MACRO_NAME, null))
                .withRenderModes(MacroRenderModesBean.newMacroRenderModesBean()
                                .withDefaultfallback(
                                        EmbeddedStaticContentMacroBean
                                                .newEmbeddedStaticContentMacroModuleBean()
                                                .withUrl("/dynamic-macro-static")
                                                .build())
                                .build()
                )
                .build();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .addJWT(installHandlerServlet)
                .setAuthenticationToNone()
                .addScope(ScopeName.ADMIN) // for using ap.request
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
                        clientSideBodyEditingMacroScriptInjection,
                        macroInTableMacro,
                        slowMacro,
                        dynamicMacroWithFallback
                )
                .addRoute(DEFAULT_MACRO_URL, ConnectAppServlets.helloWorldServlet())
                .addRoute("/render-editor", ConnectAppServlets.macroEditor())
                .addRoute("/macro-body-editor", ConnectAppServlets.macroBodyEditor(EDITED_MACRO_BODY))
                .addRoute("/macro-body-editor-script", ConnectAppServlets.macroBodyEditor(EDITED_MACRO_BODY_SCRIPT))
                .addRoute("/echo/params", ConnectAppServlets.echoQueryParametersServlet())
                .addRoute("/render-no-resize-macro", ConnectAppServlets.noResizeServlet())
                .addRoute("/images/placeholder.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .addRoute("/images/macro-icon.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .addRoute("/render-macro-in-table-macro", ConnectAppServlets.apRequestServlet())
                .addRoute("/slow-macro", new SlowMacroServlet(22))
                .addRoute("/dynamic-macro", ConnectAppServlets.helloWorldServlet())
                .addRoute("/dynamic-macro-static", ConnectAppServlets.dynamicMacroStaticServlet())
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
        ViewPage viewPage = getProduct().login(testUserFactory.basicUser().confUser(), ViewPage.class, createPageWithStorageFormatMacro());
        viewPage.getRenderedContent().getTextTimed().byDefaultTimeout();
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(SIMPLE_MACRO_KEY, 0);
        assertThat(renderedMacro.getIFrameElementText("hello-world-message"), is("Hello world"));
    }

    @Test
    public void testMacroIsRenderedForAnonymous() throws Exception
    {
        runWithAnonymousUsePermission(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                ViewPage viewPage = getProduct().viewPage(createPageWithStorageFormatMacro());
                viewPage.getRenderedContent().getTextTimed().byDefaultTimeout();
                RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(SIMPLE_MACRO_KEY, 0);
                assertThat(renderedMacro.getIFrameElementText("hello-world-message"), is("Hello world"));
                return null;
            }
        });
    }

    @Test
    public void testDynamicMacroWithPdfFallback() throws Exception
    {
        String body = new MacroStorageFormatBuilder(DYNAMIC_MACRO_KEY).build();
        Content page = createPage(randomName(DYNAMIC_MACRO_KEY), body);
        login(testUserFactory.basicUser());
        final ViewPage viewPage = getProduct().viewPage(String.valueOf(page.getId().asLong()));
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(DYNAMIC_MACRO_KEY, 0);
        String content = renderedMacro.getIFrameElementText("hello-world-message");
        assertThat(content, is("Hello world"));

        runWithAnonymousUsePermission(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                assertThat(extractPDFText(viewPage), containsString("Hello world"));
                return null;
            }
        });
    }

    @Test
    public void testDynamicMacroWithWordFallback() throws Exception
    {
        String body = new MacroStorageFormatBuilder(DYNAMIC_MACRO_KEY).build();
        Content page = createPage(randomName(DYNAMIC_MACRO_KEY), body);
        login(testUserFactory.basicUser());
        final ViewPage viewPage = getProduct().viewPage(String.valueOf(page.getId().asLong()));
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(DYNAMIC_MACRO_KEY, 0);
        String content = renderedMacro.getIFrameElementText("hello-world-message");
        assertThat(content, is("Hello world"));

        runWithAnonymousUsePermission(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                assertThat(extractWordText(viewPage), containsString("Hello world"));
                return null;
            }
        });
    }

    public String extractPDFText(ViewPage viewPage) throws IOException
    {
        String pdfUrl = viewPage.openToolsMenu().getMenuItem(By.id("action-export-pdf-link")).getHref();
        byte[] pdfData = loadDataFromUrl(pdfUrl);

        PdfReader reader = new PdfReader(pdfData);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        TextExtractionStrategy strategy;
        StringBuilder buf = new StringBuilder();
        for (int i = 1; i <= reader.getNumberOfPages(); i++)
        {
            strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
            buf.append(strategy.getResultantText());
        }
        reader.close();
        return buf.toString();
    }

    private String extractWordText(ViewPage viewPage) throws IOException
    {
        String url = viewPage.openToolsMenu().getMenuItem(By.id("action-export-word-link")).getHref();
        byte[] data = loadDataFromUrl(url);
        return new String(data);
    }

    private byte[] loadDataFromUrl(String url) throws IOException
    {
        WebDriver driver = getProduct().getTester().getDriver().getDriver();
        WebDriverSessionAwareDownloader downloader = new WebDriverSessionAwareDownloader(driver);
        return downloader.downloadBytes(url);
    }

    @Test
    public void testBodyInclusion() throws Exception
    {
        login(testUserFactory.basicUser());
        String macroBody = "a short body";
        String body = new MacroStorageFormatBuilder(SHORT_BODY_MACRO_KEY).richTextBody(macroBody).build();
        Content page = createPage(randomName(SHORT_BODY_MACRO_KEY), body);
        getProduct().viewPage(String.valueOf(page.getId().asLong()));

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(SHORT_BODY_MACRO_KEY, 0);
        assertThat(renderedMacro.getFromQueryString("body"), is("a short body"));
    }

    @Test
    public void testParameterInclusion() throws Exception
    {
        login(testUserFactory.basicUser());
        String parameterValue = "param value";
        String body = new MacroStorageFormatBuilder(PARAMETER_MACRO_KEY).parameter(SINGLE_PARAM_ID, parameterValue).build();
        Content page = createPage(randomName(PARAMETER_MACRO_KEY), body);
        getProduct().viewPage(String.valueOf(page.getId().asLong()));

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(PARAMETER_MACRO_KEY);
        assertThat(renderedMacro.getFromQueryString(SINGLE_PARAM_ID), is(parameterValue));
    }

    @Test
    public void testDescriptionShowsInMacroBrowser() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Parameter Page"));
        MacroBrowserAndEditor macroInBrowser = findMacroInBrowser(editorPage, SMALL_INLINE_MACRO_KEY);

        try
        {
            String description = macroInBrowser.macro.getItem().find(By.className("macro-desc")).timed().getText().byDefaultTimeout();
            assertThat("description shows in macro browser", description, is(SMALL_INLINE_MACRO_DESCRIPTION));
        }
        finally
        {
            // necessary to prevent Confluence from showing a navigate away alert
            selectMacroAndSave(macroInBrowser);
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testDescriptionDoesNotExposeXss() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Parameter Page"));
        MacroBrowserAndEditor macroInBrowser = findMacroInBrowser(editorPage, CLIENT_SIDE_BODY_MACRO_KEY);
        try
        {
            String description = macroInBrowser.macro.getItem().find(By.className("macro-desc")).timed().getText().byDefaultTimeout();
            assertThat("description shows in macro browser", description, is(CLIENT_SIDE_BODY_MACRO_DESCRIPTION));
        }
        finally
        {
            // necessary to prevent Confluence from showing a navigate away alert
            macroInBrowser.macro.select();
            RemotePluginDialog dialog = connectPageOperations.findDialog(CLIENT_SIDE_BODY_MACRO_KEY);
            dialog.submitAndWaitUntilHidden();
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testMultipleMacrosOnPage() throws Exception
    {
        login(testUserFactory.basicUser());
        String body = new MacroStorageFormatBuilder(SIMPLE_MACRO_KEY).build();
        Content page = createPage(randomName(SIMPLE_MACRO_KEY), body + body);
        getProduct().viewPage(String.valueOf(page.getId().asLong()));
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
        login(testUserFactory.basicUser());
        String body = new MacroStorageFormatBuilder(SMALL_INLINE_MACRO_KEY).build();
        Content page = createPage(randomName(SMALL_INLINE_MACRO_KEY), body + body);
        getProduct().viewPage(String.valueOf(page.getId().asLong()));
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(SMALL_INLINE_MACRO_KEY);
        assertThat(renderedMacro.getIFrameSize(), both(hasProperty("width", is(60))).and(hasProperty("height", is(30))));
    }

    @Test
    public void testMacroEditorSavesParameters() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Macro Editor"));
        selectMacro(editorPage, EDITOR_MACRO_NAME, macroDialogSubmitter(EDITOR_MACRO_KEY));

        editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(EDITOR_MACRO_KEY);
        String content = renderedMacro.getIFrameElementText("footy");

        assertThat(content, is("footy: American Football"));
    }

    @Test
    public void testMacroInOrderedTable() throws Exception
    {
        login(testUserFactory.basicUser());
        Content page = createPage(randomName(TABLE_MACRO_NAME), loadResourceAsString("confluence/test-page-table-macro.xhtml"));
        getProduct().viewPage(String.valueOf(page.getId().asLong()));

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(TABLE_MACRO_KEY);
        renderedMacro.waitUntilContentElementNotEmpty("client-http-status");
        assertThat(renderedMacro.getIFrameElementText("client-http-status"), is("200"));

        connectPageOperations.reorderConfluenceTableOnPage();
        RenderedMacro refreshedMacro = connectPageOperations.findMacroWithIdPrefix(TABLE_MACRO_KEY);
        refreshedMacro.waitUntilContentElementNotEmpty("client-http-status");
        assertThat(refreshedMacro.getIFrameElementText("client-http-status"), is("200"));
    }

    @Test
    public void testMacroEditorCanReadBody() throws Exception
    {
        String macroBody = "cat pictures go here";
        String body = new MacroStorageFormatBuilder(CLIENT_SIDE_BODY_MACRO_KEY).richTextBody(macroBody).build();
        Content page = createPage(randomName(CLIENT_SIDE_BODY_MACRO_KEY), body);
        EditContentPage editorPage = getProduct().loginAndEdit(testUserFactory.basicUser().confUser(), new Page(page.getId().asLong()));

        RemotePluginDialog dialog = connectPageOperations.editMacro(CLIENT_SIDE_BODY_MACRO_KEY);
        try
        {
            String content = dialog.getValueById("macro-body");
            assertThat(content, is(macroBody));
        }
        finally
        {
            dialog.cancelAndWaitUntilHidden();
            cancelEditor(editorPage);
        }
    }

    @Test
    public void testMacroEditorCanWriteBody() throws Exception
    {
        String body = new MacroStorageFormatBuilder(CLIENT_SIDE_BODY_MACRO_KEY).build();
        Content page = createPage(randomName(CLIENT_SIDE_BODY_MACRO_KEY), body);
        EditContentPage editorPage = getProduct().loginAndEdit(testUserFactory.basicUser().confUser(), new Page(page.getId().asLong()));
        RemotePluginDialog dialog = connectPageOperations.editMacro(CLIENT_SIDE_BODY_MACRO_KEY);
        dialog.submitAndWaitUntilHidden();
        editorPage.save();

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(CLIENT_SIDE_BODY_MACRO_KEY);
        assertThat(renderedMacro.getIFrameElementText("body"), is("body: " + EDITED_MACRO_BODY));
        assertThat(renderedMacro.getFromQueryString("cv"), isVersionNumber());
    }

    @Test
    public void testBodyIsSanitized() throws Exception
    {
        String body = new MacroStorageFormatBuilder(CLIENT_SIDE_BODY_MACRO_SCRIPT_KEY).build();
        Content page = createPage(randomName(CLIENT_SIDE_BODY_MACRO_SCRIPT_NAME), body);
        EditContentPage editorPage = getProduct().loginAndEdit(testUserFactory.basicUser().confUser(), new Page(page.getId().asLong()));

        RemotePluginDialog dialog = connectPageOperations.editMacro(CLIENT_SIDE_BODY_MACRO_SCRIPT_KEY);
        dialog.submitAndWaitUntilHidden();
        editorPage.save();

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(CLIENT_SIDE_BODY_MACRO_SCRIPT_KEY);
        assertThat(renderedMacro.getIFrameElementText("body"), is("body: <strong>must</strong> be removed:"));
    }

    @Test
    public void testSlowMacro() throws Exception
    {
        login(testUserFactory.basicUser());
        String body = new MacroStorageFormatBuilder(SLOW_MACRO_KEY).build();
        String title = randomName(SLOW_MACRO_KEY);
        createPage(title, body);
        ConfluencePageWithRemoteMacro pageWithRemoteMacro = product.visit(ConfluencePageWithRemoteMacro.class, title, SLOW_MACRO_KEY);
        assertThat(pageWithRemoteMacro.macroHasTimedOut(), is(true));
    }

    @Test
    public void testMacroInComment() throws Exception
    {
        login(testUserFactory.basicUser());
        String title = randomName("The macro is in the comment!");
        Content page = createPage(title, "The macro is in the comment!");
        addCommentWithMacro(String.valueOf(page.getId().asLong()));
        product.visit(ConfluencePageWithRemoteMacro.class, title, SIMPLE_MACRO_KEY);

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(SIMPLE_MACRO_KEY, 0);
        String content = renderedMacro.getIFrameElementText("hello-world-message");

        assertThat(content, is("Hello world"));
    }

    @Override
    protected String getAddonBaseUrl()
    {
        return remotePlugin.getAddon().getBaseUrl();
    }

    private String createPageWithStorageFormatMacro()
    {
        String body = new MacroStorageFormatBuilder(SIMPLE_MACRO_KEY).build();
        Content page = createPage(randomName(SIMPLE_MACRO_KEY), body);
        return String.valueOf(page.getId().asLong());
    }

    private static final class SlowMacroServlet extends HttpServlet
    {
        private final int seconds;

        private SlowMacroServlet(int seconds)
        {
            this.seconds = seconds;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            final long wakeTime = System.currentTimeMillis() + seconds * 1000 + 1;

            while (System.currentTimeMillis() < wakeTime)
            {
                try
                {
                    Thread.sleep(wakeTime - System.currentTimeMillis());
                }
                catch (InterruptedException e)
                {
                    // do nothing
                }
            }

            resp.setContentType("text/html");
            resp.getWriter().write("finished");
            resp.getWriter().close();
        }
    }
}