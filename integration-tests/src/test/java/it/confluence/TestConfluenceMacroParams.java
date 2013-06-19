package it.confluence;

import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceMacroPage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceMacroTestSuitePage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceOps;
import com.atlassian.plugin.remotable.test.confluence.FixedConfluenceTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import redstone.xmlrpc.XmlRpcFault;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import static com.atlassian.plugin.remotable.test.Utils.loadResourceAsString;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestConfluenceMacroParams
{

    private static final TestedProduct<WebDriverTester> product;
    private static final ConfluenceOps confluenceOps;
    static
    {
        System.setProperty("testedProductClass", FixedConfluenceTestedProduct.class.getName());
        product = OwnerOfTestedProduct.INSTANCE;
        confluenceOps = new ConfluenceOps(product.getProductInstance().getBaseUrl());
    }

    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
	public void testContextParam() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        Map<String,String> params = product.visit(ConfluenceMacroTestSuitePage.class, pageData.get("title"))
                                          .visitGeneralLink()
                                          .getIframeQueryParams();

        assertEquals(pageData.get("id"), params.get("page_id"));
	}

    @Test
    public void testMacroWithHeaderParams() throws Exception, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test",
                "<div class=\"header-macro\">\n" +
                "   <ac:macro ac:name=\"header\" />\n" +
                "</div>");

        MyParamsMacroServlet macroServlet = new MyParamsMacroServlet();
        RemotePluginRunner runner = new RemotePluginRunner(product.getProductInstance().getBaseUrl(), "header")
                .addMacro("header", "/header", macroServlet, asList(
                    asList("page_id", "query"),
                    asList("user_id", "header")
                ))
                .start();
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        product.visit(ConfluenceMacroPage.class, pageData.get("title"));
        assertEquals(pageData.get("id"), macroServlet.getQueryParams().get("page_id"));
        assertFalse(macroServlet.getQueryParams().containsKey("user_id"));
        assertEquals("admin", macroServlet.getHeaderParams().get("user_id"));
        assertFalse(macroServlet.getHeaderParams().containsKey("page_id"));
        runner.stop();
    }

    public static class MyParamsMacroServlet extends HttpServlet
    {
        private Map<String,String> headerParams = newHashMap();
        private Map<String,String> queryParams = newHashMap();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            for (Enumeration<String> names = req.getHeaderNames(); names.hasMoreElements(); )
            {
                String rawName = names.nextElement();
                if (rawName.startsWith("AP-CTX-"))
                {
                    String name = rawName.substring("AP-CTX-".length()).replace('-', '_').toLowerCase();
                    String value = req.getHeader(rawName);
                    headerParams.put(name, value);
                }
            }

            for (Enumeration<String> names = req.getParameterNames(); names.hasMoreElements(); )
            {
                String rawName = names.nextElement();
                if (rawName.startsWith("ctx_"))
                {
                    String name = rawName.substring("ctx_".length());
                    String value = req.getParameter(rawName);
                    queryParams.put(name, value);
                }
            }
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");
            out.write("<p>hi</p>");
            out.close();
        }

        public Map<String, String> getHeaderParams()
        {
            return headerParams;
        }

        public Map<String, String> getQueryParams()
        {
            return queryParams;
        }
    }

}
