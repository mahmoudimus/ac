package it.modules.confluence;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.BaseUrlLocator;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluencePageWithRemoteMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import it.servlet.ConnectAppServlets;
import it.servlet.EchoContextServlet;
import it.servlet.EchoQueryParametersServlet;
import it.util.TestUser;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import redstone.xmlrpc.XmlRpcFault;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.both;
import static org.junit.Assert.assertThat;
import static it.matcher.ParamMatchers.isVersionNumber;

public class TestStaticContentMacro extends AbstractContentMacroTest
{
    private static final String STORAGE_FORMAT_MACRO_NAME = "Storage Format Macro";
    private static final String STORAGE_FORMAT_MACRO_KEY = "storage-format-macro";
    private static final String COUNTER_CSS_CLASS = "rp-counter";

    private static final String GET_MACRO_NAME = "Get Macro";
    private static final String GET_MACRO_KEY = "get-macro";

    private static final String COUNTER_MACRO_NAME = "Counter Macro";
    private static final String COUNTER_MACRO_KEY = "counter-macro";

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

        StaticContentMacroModuleBean counterMacro = newStaticContentMacroModuleBean()
                .withKey(COUNTER_MACRO_KEY)
                .withUrl("/counter")
                .withName(new I18nProperty(COUNTER_MACRO_NAME, null))
                .build();

        parameterServlet = new EchoQueryParametersServlet();
        contextServlet = new EchoContextServlet();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addJWT()
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
                        hiddenMacro,
                        counterMacro
                )
                .addRoute(DEFAULT_MACRO_URL, ConnectAppServlets.wrapContextAwareServlet(parameterServlet))
                .addRoute("/render-editor", ConnectAppServlets.macroEditor())
                .addRoute("/echo/params", ConnectAppServlets.echoQueryParametersServlet())
                .addRoute("/render-context", ConnectAppServlets.wrapContextAwareServlet(contextServlet))
                .addRoute("/images/placeholder.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .addRoute("/render-storage-format", ConnectAppServlets.resourceServlet("confluence/test-static-content-macro.xhtml", "application/xhtml+xml"))
                .addRoute("/counter", new CounterMacroServlet())
                .addScope(ScopeName.WRITE)
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
    public void testMacroInComment() throws MalformedURLException, XmlRpcFault
    {
        addSimpleMacroToComment();
        final WebElement commentBody = connectPageOperations.findElementByClass("comment-content");
        String commentText = commentBody.getText();
        String[] lines = StringUtils.split(commentText, "\n");

        Option<String> maybeVersion = Iterables.findFirst(Lists.newArrayList(lines), new Predicate<String>(){
            @Override
            public boolean apply(String line)
            {
                return line.startsWith("cv:");
            }});

        String version = maybeVersion.get().replaceFirst("cv:", "").trim();
        assertThat(version, isVersionNumber());
        assertThat(commentBody.getText(), both(startsWith("Hello world!!")).and(containsString("xdm_c: channel-" + SIMPLE_MACRO_KEY)));
    }

    @Test
    public void testMacroCacheFlushes() throws Exception
    {
        final String title = randomName("Counter Page");
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        editorPage.setTitle(title);
        selectMacroAndSave(editorPage, COUNTER_MACRO_NAME);
        editorPage.save();

        ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, title, COUNTER_MACRO_NAME);
        assertThat(getCounter(page), is(0));

        // stays the same on a new visit
        page = product.visit(ConfluencePageWithRemoteMacro.class, title, COUNTER_MACRO_NAME);
        assertThat(getCounter(page), is(0));

        clearCaches();

        page = product.visit(ConfluencePageWithRemoteMacro.class, title, COUNTER_MACRO_NAME);
        assertThat(getCounter(page), is(1));
    }

    private static void clearCaches() throws Exception
    {
        final URL url = new URL(BaseUrlLocator.getBaseUrl() + "/rest/atlassian-connect/latest/macro/app/" + remotePlugin.getAddon().getKey());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        remotePlugin.getSignedRequestHandler().sign(url.toURI(), "DELETE", null, conn);
        conn.connect();
        int code = conn.getResponseCode();
        conn.disconnect();

        if (code < 200 || code >= 300)
        {
            throw new RuntimeException(String.format("Macro cache flush request 'DELETE %s' returned %d", url, code));
        }
    }

    @Override
    protected String getAddonBaseUrl()
    {
        return remotePlugin.getAddon().getBaseUrl();
    }

    private void testMacroIsRendered(User user) throws Exception
    {
        ViewPage page = getMacroContent(user, STORAGE_FORMAT_MACRO_NAME, "Storage format macro");
        String content = page.getRenderedContent().getTextTimed().byDefaultTimeout();
        assertThat(content, endsWith("Storage Format Content"));
    }

    private int getCounter(ConfluencePageWithRemoteMacro page)
    {
        return Integer.valueOf(page.getText(COUNTER_CSS_CLASS));
    }

    private static final class CounterMacroServlet extends HttpServlet
    {
        private static final long ONE_YEAR_SECONDS = 60L * 60L * 24L * 365L;
        private static final long ONE_YEAR_MILLISECONDS = 1000 * ONE_YEAR_SECONDS;
        private int counter = 0;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("text/html");
            resp.setDateHeader("Expires", System.currentTimeMillis() + ONE_YEAR_MILLISECONDS);
            resp.setHeader("Cache-Control", "s-maxage=" + ONE_YEAR_SECONDS);
            PrintWriter writer = resp.getWriter();
            writer.print("<div>Counter: <span class=\"" + COUNTER_CSS_CLASS + "\">" + counter + "</span></div>");
            writer.close();
            ++counter;
        }
    }
}
