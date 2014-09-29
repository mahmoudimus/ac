package it.modules.confluence;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluencePageWithRemoteMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import it.servlet.EchoContextServlet;
import it.servlet.EchoQueryParametersServlet;
import it.util.TestUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestStaticContentMacro extends AbstractContentMacroTest
{
    private static final String STORAGE_FORMAT_MACRO_NAME = "Storage Format Macro";
    private static final String STORAGE_FORMAT_MACRO_KEY = "storage-format-macro";
    private static final String COUNTER = "rp-counter";

    private static final String GET_MACRO_NAME = "Get Macro";
    private static final String GET_MACRO_KEY = "get-macro";

    private static ConnectRunner remotePlugin;
    private static EchoQueryParametersServlet parameterServlet;
    private static EchoContextServlet contextServlet;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        StaticContentMacroModuleBean simpleMacro = createSimpleMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean allParameterTypesMacro = createAllParametersMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean featuredMacro = createFeaturedMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean imagePlaceholderMacro = createImagePlaceholderMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean longBodyMacro = createLongBodyMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean shortBodyMacro = createShortBodyMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean parameterMacro = createParameterMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean editorMacro = createEditorMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean customTitleEditorMacro = createCustomEditorTitleMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean hiddenMacro = createHiddenMacro(newStaticContentMacroModuleBean());

        StaticContentMacroModuleBean storageFormatMacro = newStaticContentMacroModuleBean()
                .withUrl("/render-storage-format")
                .withKey(STORAGE_FORMAT_MACRO_KEY)
                .withName(new I18nProperty(STORAGE_FORMAT_MACRO_NAME, ""))
                .build();

        StaticContentMacroModuleBean getMacro = newStaticContentMacroModuleBean()
                .withUrl("/render-context")
                .withKey(GET_MACRO_KEY)
                .withName(new I18nProperty(GET_MACRO_NAME, ""))
                .build();

        parameterServlet = new EchoQueryParametersServlet();
        contextServlet = new EchoContextServlet();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addModules("staticContentMacros",
                        simpleMacro,
                        allParameterTypesMacro,
                        longBodyMacro,
                        shortBodyMacro,
                        featuredMacro,
                        imagePlaceholderMacro,
                        parameterMacro,
                        storageFormatMacro,
                        getMacro,
                        editorMacro,
                        customTitleEditorMacro,
                        hiddenMacro
                )
                .addRoute(DEFAULT_MACRO_URL, ConnectAppServlets.wrapContextAwareServlet(parameterServlet))
                .addRoute("/render-editor", ConnectAppServlets.macroEditor())
                .addRoute("/echo/params", ConnectAppServlets.echoQueryParametersServlet())
                .addRoute("/render-context", ConnectAppServlets.wrapContextAwareServlet(contextServlet))
                .addRoute("/images/placeholder.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .addRoute("/render-storage-format", ConnectAppServlets.resourceServlet("confluence/test-static-content-macro.xhtml", "application/xhtml+xml"))
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
        testMacroIsRendered(TestUser.ADMIN.confUser());
    }

    @Test
    public void testMacroIsRenderedForAnonymous() throws Exception
    {
        testMacroIsRendered(null);
    }

    @Test
    public void testMacroHttpMethod() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("HTTP GET Macro"));

        selectMacroAndSave(editorPage, GET_MACRO_NAME);

        savedPage = editorPage.save();

        assertThat(String.valueOf(contextServlet.waitForContext().get("req_method")), is("GET"));
    }

    @Test
    public void testBodyInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Short Body Macro"));

        selectMacroAndSave(editorPage, SHORT_BODY_MACRO_NAME);

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getEditor().getContent();
        editorContent.setRichTextMacroBody("a short body");

        savedPage = editorPage.save();

        String body = parameterServlet.waitForQueryParameters().any("body").getValue();
        assertThat(body, is("<p>a short body</p>"));
    }

    @Test
    public void testBodyHashInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Long Body Macro"));

        selectMacroAndSave(editorPage, LONG_BODY_MACRO_NAME);

        String body = StringUtils.repeat("x ", 200);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getEditor().getContent();
        editorContent.setPlainTextMacroBody(body);

        savedPage = editorPage.save();

        String hash = parameterServlet.waitForQueryParameters().any("hash").getValue();
        assertThat(hash, is(DigestUtils.md5Hex(body)));
    }

    @Test
    public void testParameterInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        editorPage.setTitle(randomName("Parameter Page"));
        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, PARAMETER_MACRO_NAME);

        macroBrowserAndEditor.macroForm.getAutocompleteField("param1").setValue("param value");
        macroBrowserAndEditor.browserDialog.clickSave();

        savedPage = editorPage.save();

        String value = parameterServlet.waitForQueryParameters().any("param1").getValue();
        assertThat(value, is("param value"));
    }

    @Test
    @Ignore
    public void testMacroCacheFlushes() throws Exception
    {
        /*
        final ConfluenceOps.ConfluencePageData pageData = createPage(TestUser.ADMIN, pageWithMacro(COUNTER_MACRO));

        counterMacroServlet.reset();

        ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), COUNTER_MACRO);

        //TODO: this is flaky, where sometimes the counter value in the page is inexplicably 0
        assertEquals(1, getCounter(page));

        // stays the same on a new visit
        page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), COUNTER_MACRO);
        assertEquals(1, getCounter(page));

        clearCaches();

        page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), COUNTER_MACRO);
        assertEquals(2, getCounter(page));
        */
    }

    private static void clearCaches() throws Exception
    {
        final URL url = new URL(product.getProductInstance().getBaseUrl() + "/rest/atlassian-connect/latest/macro/app/" + remotePlugin.getAddon().getKey());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        remotePlugin.getSignedRequestHandler().sign(url.toURI(), "DELETE", null, conn);
        int code = conn.getResponseCode();
        System.out.println("Reset from " + product.getProductInstance().getBaseUrl() + " returned: " + code);
        conn.disconnect();
    }

    @Override
    protected String getAddonBaseUrl()
    {
        return remotePlugin.getAddon().getBaseUrl();
    }

    private void testMacroIsRendered(User user) throws Exception
    {
        ViewPage page = getMacroContent(user, STORAGE_FORMAT_MACRO_NAME, "Storage format macro");
        String content = page.getRenderedContent().getText();
        assertThat(content, is("Storage Format Content"));
    }

    private int getCounter(ConfluencePageWithRemoteMacro page)
    {
        return Integer.valueOf(page.getText(COUNTER));
    }

    private static final class CounterMacroServlet extends HttpServlet
    {
        private static final long ONE_YEAR_SECONDS = 60L * 60L * 24L * 365L;
        private static final long ONE_YEAR_MILLISECONDS = 1000 * ONE_YEAR_SECONDS;
        private static final int INITIAL_VALUE = 0;
        private int counter = INITIAL_VALUE;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("text/html");
            resp.setDateHeader("Expires", System.currentTimeMillis() + ONE_YEAR_MILLISECONDS);
            resp.setHeader("Cache-Control", "s-maxage=" + ONE_YEAR_SECONDS);
            PrintWriter writer = resp.getWriter();
            writer.print("<div>Counter: <span class=\"" + COUNTER + "\">" + counter++ + "</span></div>");
            writer.close();
        }

        private void reset()
        {
            this.counter = INITIAL_VALUE;
        }
    }
}
