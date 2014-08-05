package it.confluence;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.OAuthUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluencePageMacroPage;
import com.atlassian.plugin.connect.test.server.XMLAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.MacroPageModule;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.HttpUtils.renderHtml;
import static com.atlassian.plugin.connect.test.Utils.loadResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@XmlDescriptor
public final class TestConfluenceInlinePageMacro extends ConfluenceWebDriverTestBase
{
    private static XMLAddOnRunner remotePlugin;

    @BeforeClass
    public static void setupConfluenceAndStartConnectAddOn() throws Exception
    {
        remotePlugin = new XMLAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .addPermission("read_content")
                .addPermission("read_users_and_groups")
                .addPermission("read_server_information")
                .add(MacroPageModule.key("app1-page")
                        .name("app1-page")
                        .path("/ap")
                        .outputType("inline")
                        .bodyType("none")
                        .resource(ConnectAppServlets.apRequestServlet()))
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
    public void testInlinePageMacro() throws XmlRpcFault, IOException
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(TestUser.ADMIN), "ds", "testinlineMacro", loadResourceAsString("confluence/test-page-macro.xhtml"));

        ConfluencePageMacroPage page = loginAndVisit(TestUser.BETTY, ConfluencePageMacroPage.class, pageData.getTitle(), "app1-page-0");

        assertTrue(page.getContainerDiv().getAttribute("class").contains("ap-inline"));
        assertEquals("Success", page.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), page.getConsumerKey());
    }

    public static final class MyMacroServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            final String pageId = req.getParameter("ctx_page_id");
            final String favoriteFooty = req.getParameter("footy");
            final String body = req.getParameter("body");
            resp.setDateHeader("Expires", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));
            resp.setHeader("Cache-Control", "public");

            renderHtml(resp, "confluence/macro/extended.mu", new HashMap<String, Object>()
            {{
                    put("pageId", pageId);
                    put("footy", favoriteFooty);
                    put("body", body);
                    put("server", "???");
                }});
        }
    }
}
