package it.confluence;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Suppliers;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluencePageWithRemoteMacro;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.*;
import it.servlet.ConnectAppServlets;
import it.servlet.macro.SimpleMacroServlet;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps.ConfluenceUser;
import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newServlet;
import static com.google.common.base.Strings.nullToEmpty;
import static it.util.TestConstants.ADMIN_USERNAME;
import static it.confluence.ContextParameters.*;
import static java.lang.String.format;
import static org.junit.Assert.*;

@XmlDescriptor
public final class TestConfluenceRemoteMacro extends ConfluenceWebDriverTestBase
{
    private static final Option<ConfluenceUser> ADMIN_CONFLUENCE_USER = some(new ConfluenceUser(ADMIN_USERNAME, ADMIN_USERNAME));
    private static final Option<ConfluenceUser> ANONYMOUS_CONFLUENCE_USER = none(ConfluenceUser.class);

    private static final String SIMPLE_MACRO = "simple-macro";
    private static final String HEADER_MACRO = "header-macro";
    private static final String POST_MACRO = "post-macro";
    private static final String COUNTER_MACRO = "counter-macro";
    private static final String SLOW_MACRO = "slow-macro";
    private static final String EXTENDED_MACRO = "extended-macro";

    private static final String SIMPLE_MACRO_PATH = "/simple-macro";
    private static final String HEADER_MACRO_PATH = "/header-macro";
    private static final String POST_MACRO_PATH = "/post-macro";
    private static final String COUNTER = "rp-counter";

    private static final String REQUEST_METHOD = "req_method";
    private static final String REQUEST_URI = "req_uri";
    private static final String REQUEST_QUERY = "req_query";
    private static final String SPACE_KEY = "ds";

    private static AtlassianConnectAddOnRunner remotePlugin;
    private static CounterMacroServlet counterMacroServlet;

    @BeforeClass
    public static void setupAndStartConnectAddOn() throws Exception
    {
        counterMacroServlet = new CounterMacroServlet();

        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(newSimpleRemoteMacroModule(SIMPLE_MACRO, SIMPLE_MACRO_PATH))
                .add(newSimpleRemoteMacroModule(HEADER_MACRO, HEADER_MACRO_PATH)
                        .contextParameters(
                                ContextParameter.name(CTX_OUTPUT_TYPE).header(),
                                ContextParameter.name(CTX_PAGE_ID).header(),
                                ContextParameter.name(CTX_PAGE_TYPE).header(),
                                ContextParameter.name(CTX_PAGE_TITLE).header(),
                                ContextParameter.name(CTX_SPACE_KEY).header(),
                                ContextParameter.name(CTX_USER_ID).header(),
                                ContextParameter.name(CTX_USER_KEY).header())
                        .resource(newServlet(new HeaderMacroServlet())))
                .add(newSimpleRemoteMacroModule(POST_MACRO, POST_MACRO_PATH).method("POST"))
                .add(newSimpleRemoteMacroModule(SLOW_MACRO, "/slow-macro").resource(new SlowMacroServlet(22)))
                .add(newSimpleRemoteMacroModule(COUNTER_MACRO, "/counter-macro").resource(counterMacroServlet))
                .add(newSimpleRemoteMacroModule(EXTENDED_MACRO, "/extended-macro")
                        .iconUrl("/public/sandcastles.jpg")
                        .bodyType("rich-text")
                        .featured("true")
                        .category(MacroCategory.name("development"))
                        .parameters(MacroParameter.name("footy")
                                                  .title("Favorite Footy")
                                                  .type("enum")
                                                  .required("true")
                                                  .values("American Football", "Soccer", "Rugby Union", "Rugby League"))
                        .editor(MacroEditor.at("/extended-macro-editor")
                                .height("600")
                                .width("600")
                                .resource(ConnectAppServlets.macroEditor()))
                        .resource(ConnectAppServlets.macroExtended()))
                .start();
    }

    private static RemoteMacroModule newSimpleRemoteMacroModule(String name, String path)
    {
        return RemoteMacroModule.key(name)
                                .name(name)
                                .title("Atlassian Connect Remote Macro: " + name)
                                .path(path)
                                .outputBlock()
                                .contextParameters(
                                        ContextParameter.name(CTX_OUTPUT_TYPE).query(),
                                        ContextParameter.name(CTX_PAGE_ID).query(),
                                        ContextParameter.name(CTX_PAGE_TYPE).query(),
                                        ContextParameter.name(CTX_PAGE_TITLE).query(),
                                        ContextParameter.name(CTX_USER_ID).query(),
                                        ContextParameter.name(CTX_SPACE_KEY).query(),
                                        ContextParameter.name(CTX_USER_KEY).query()
                                )
                                .resource(ConnectAppServlets.macroSimple());
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
    public void testSimpleMacro() throws Exception
    {
        final ConfluenceOps.ConfluencePageData pageData = createPage(ADMIN_CONFLUENCE_USER, pageWithMacro(SIMPLE_MACRO));
        final ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), SIMPLE_MACRO);

        assertEquals(SIMPLE_MACRO_PATH, page.getText(REQUEST_URI));
        assertTrue(StringUtils.isNotBlank(page.getText(REQUEST_QUERY)));
        assertEquals("GET", page.getText(REQUEST_METHOD));

        assertEquals("display", page.getText(CTX_OUTPUT_TYPE));
        assertEquals(pageData.getId(), page.getText(CTX_PAGE_ID));
        assertEquals("page", page.getText(CTX_PAGE_TYPE));
        assertEquals(pageData.getTitle(), page.getText(CTX_PAGE_TITLE));
        assertEquals(SPACE_KEY, page.getText(CTX_SPACE_KEY));
        // the macro is being viewed by an anonymous user
        assertNull(page.getText(CTX_USER_ID));
        assertNull(page.getText(CTX_USER_KEY));
    }

    @Test
    public void testSimpleMacroUsingHeaderParams() throws Exception
    {
        final ConfluenceOps.ConfluencePageData pageData = createPage(ADMIN_CONFLUENCE_USER, pageWithMacro(HEADER_MACRO));
        final ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), HEADER_MACRO);

        assertEquals(HEADER_MACRO_PATH, page.getText(REQUEST_URI));
        assertTrue(StringUtils.isNotBlank(page.getText(REQUEST_QUERY)));
        assertEquals("GET", page.getText(REQUEST_METHOD));

        assertEquals("display", page.getText(CTX_OUTPUT_TYPE));
        assertEquals(pageData.getId(), page.getText(CTX_PAGE_ID));
        assertEquals("page", page.getText(CTX_PAGE_TYPE));
        assertEquals(pageData.getTitle(), page.getText(CTX_PAGE_TITLE));

        assertEquals(SPACE_KEY, page.getText(CTX_SPACE_KEY));
        // the macro is being viewed by an anonymous user
        assertNull(page.getText(CTX_USER_ID));
        assertNull(page.getText(CTX_USER_KEY));
    }

    @Test
    public void testSimpleMacroUsingPost() throws Exception
    {
        final ConfluenceOps.ConfluencePageData pageData = createPage(ADMIN_CONFLUENCE_USER, pageWithMacro(POST_MACRO));
        final ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), POST_MACRO);

        assertEquals(POST_MACRO_PATH, page.getText(REQUEST_URI));
        assertTrue(StringUtils.isEmpty(page.getText(REQUEST_QUERY)));
        assertEquals("POST", page.getText(REQUEST_METHOD));

        assertEquals("display", page.getText(CTX_OUTPUT_TYPE));
        assertEquals(pageData.getId(), page.getText(CTX_PAGE_ID));
        assertEquals("page", page.getText(CTX_PAGE_TYPE));
        assertEquals(pageData.getTitle(), page.getText(CTX_PAGE_TITLE));
        assertEquals(SPACE_KEY, page.getText(CTX_SPACE_KEY));
        // the macro is being viewed by an anonymous user
        assertNull(page.getText(CTX_USER_ID));
        assertNull(page.getText(CTX_USER_KEY));
    }

    @Test
    public void testAnonymousMacro() throws XmlRpcFault, IOException
    {
        final ConfluenceOps.ConfluencePageData pageData = createPage(ANONYMOUS_CONFLUENCE_USER, pageWithMacro(SIMPLE_MACRO));
        final ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), SIMPLE_MACRO);

        assertEquals(SIMPLE_MACRO_PATH, page.getText(REQUEST_URI));
        assertTrue(StringUtils.isNotBlank(page.getText(REQUEST_QUERY)));
        assertEquals("GET", page.getText(REQUEST_METHOD));

        assertEquals("display", page.getText(CTX_OUTPUT_TYPE));
        assertEquals(pageData.getId(), page.getText(CTX_PAGE_ID));
        assertEquals("page", page.getText(CTX_PAGE_TYPE));
        assertEquals(SPACE_KEY, page.getText(CTX_SPACE_KEY));
        assertEquals(pageData.getTitle(), page.getText(CTX_PAGE_TITLE));
        // the macro is being viewed by an anonymous user
        assertNull(page.getText(CTX_USER_ID));
        assertNull(page.getText(CTX_USER_KEY));
    }

    @Test
    public void testMacroInComment() throws XmlRpcFault, IOException
    {
        final ConfluenceOps.ConfluencePageData pageData = createPage(ADMIN_CONFLUENCE_USER, "The macro is in the comment!");
        final ConfluenceOps.ConfluenceCommentData commentData = confluenceOps.addComment(ADMIN_CONFLUENCE_USER, pageData.getId(), pageWithMacro(SIMPLE_MACRO));
        final ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), SIMPLE_MACRO);

        assertEquals(SIMPLE_MACRO_PATH, page.getText(REQUEST_URI));
        assertTrue(StringUtils.isNotBlank(page.getText(REQUEST_QUERY)));
        assertEquals("GET", page.getText(REQUEST_METHOD));

        assertEquals("display", page.getText(CTX_OUTPUT_TYPE));
        assertEquals(commentData.getId(), page.getText(CTX_PAGE_ID));
        assertEquals("comment", page.getText(CTX_PAGE_TYPE));
        assertNull(page.getText(CTX_PAGE_TITLE));
        assertEquals(SPACE_KEY, page.getText(CTX_SPACE_KEY));
        // the macro is being viewed by an anonymous user
        assertNull(page.getText(CTX_USER_ID));
        assertNull(page.getText(CTX_USER_KEY));
    }

    @Test
    @Ignore
    public void testMacroCacheFlushes() throws Exception
    {
        final ConfluenceOps.ConfluencePageData pageData = createPage(ADMIN_CONFLUENCE_USER, pageWithMacro(COUNTER_MACRO));

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
    }

    @Test // this one is not actually using the editor yet
    public void testMacroWithEditor() throws Exception
    {
        final ConfluenceOps.ConfluencePageData pageData = createPage(ANONYMOUS_CONFLUENCE_USER,
                format("<div class=\"%1$s\"><ac:macro ac:name=\"%1$s\">\n" +
                        "    <ac:parameter ac:name=\"footy\">Soccer</ac:parameter>\n" +
                        "    <ac:rich-text-body><p>outside note</p>\n" +
                        "        <ac:macro ac:name=\"note\">\n" +
                        "            <ac:rich-text-body><p>some note</p></ac:rich-text-body>\n" +
                        "        </ac:macro>\n" +
                        "    </ac:rich-text-body>\n" +
                        "</ac:macro></div>", EXTENDED_MACRO));

        final ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, pageData.getTitle(), EXTENDED_MACRO);

        assertEquals("Soccer", page.getText("footy"));
        assertTrue(page.getText("body").contains("outside note"));
        assertTrue(page.getText("body").contains("some note"));
    }

    private String pageWithMacro(String macroName)
    {
        return format("<div class=\"%1$s\"><ac:macro ac:name=\"%1$s\" /></div>", macroName);
    }

    private int getCounter(ConfluencePageWithRemoteMacro page)
    {
        return Integer.valueOf(page.getText(COUNTER));
    }

    @Test
    public void testSlowMacro() throws Exception
    {
        final ConfluenceOps.ConfluencePageData confluencePageData = createPage(ADMIN_CONFLUENCE_USER, pageWithMacro(SLOW_MACRO));
        final ConfluencePageWithRemoteMacro page = product.visit(ConfluencePageWithRemoteMacro.class, confluencePageData.getTitle(), SLOW_MACRO);
        assertTrue("The macro should have timed out.", page.macroHasTimedOut());
    }


    private static ConfluenceOps.ConfluencePageData createPage(Option<ConfluenceUser> user, String content) throws XmlRpcFault, IOException
    {
        return confluenceOps.setPage(user, SPACE_KEY, "test", content);
    }

    private static void clearCaches() throws Exception
    {
        final URL url = new URL(product.getProductInstance().getBaseUrl() + "/rest/atlassian-connect/latest/macro/app/" + remotePlugin.getPluginKey());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        try
        {
            remotePlugin.getSignedRequestHandler().getOrError(Suppliers.ofInstance("No signed request handler")).sign(url.toURI(), "DELETE", null, conn);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        int code = conn.getResponseCode();
        System.out.println("Reset from " + product.getProductInstance().getBaseUrl() + " returned: " + code);
        conn.disconnect();
    }

    private static final class HeaderMacroServlet extends SimpleMacroServlet
    {
        @Override
        protected String getParam(HttpServletRequest req, String name)
        {
            return nullToEmpty(req.getHeader("AP-CTX-" + name.replace("_", "-")));
        }
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
            try
            {
                Thread.sleep(seconds * 1000);
            }
            catch (InterruptedException e)
            {
                // do nothing
            }
            resp.setContentType("text/html");
            resp.getWriter().write("finished");
            resp.getWriter().close();
        }
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
