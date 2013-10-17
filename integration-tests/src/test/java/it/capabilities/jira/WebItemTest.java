package it.capabilities.jira;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;

import com.google.common.base.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.TestPageModules;
import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BETTY_USERNAME;
import static java.lang.String.valueOf;
import static org.junit.Assert.*;


/**
 * @since version
 */
public class WebItemTest extends JiraWebDriverTestBase
{
    private static final String ADDON_WEBITEM = "ac-general-web-item";
    private static final String PRODUCT_WEBITEM = "quick-project-link";
    private static final String ABSOLUTE_WEBITEM = "google-link";

    private static ConnectCapabilitiesRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(),"my-plugin")
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("AC General Web Item", "ac.gen"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("/irwi")
                        .build())
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("Quick project link","ac.qp"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink(product.getProductInstance().getBaseUrl() + "/browse/ACDEV-1234")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build()
                                ,newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                        )
                        .build())
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("google link","ac.gl"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("http://www.google.com")
                        .build())
                
                .addRoute("/onlyBettyCondition",new OnlyBettyConditionServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Test
    public void testAbsoluteWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ABSOLUTE_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        webItem.click();

        assertTrue("Web item link should be absolute", webItem.isAbsolute());
        assertEquals("http://www.google.com", webItem.getPath());
    }

    @Test
    public void bettyCanSeeWebItem()
    {
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(PRODUCT_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(PRODUCT_WEBITEM, Optional.<String>absent());
        assertNull("Web item should NOT be found", webItem);
    }

    public static final class OnlyBettyConditionServlet extends HttpServlet
    {
        private static final String BETTY = "betty";

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            final String loggedInUser = req.getParameter("user_id");
            final boolean isBetty = isBetty(loggedInUser);

            logger.debug("The logged in user is {}betty, their user key is '{}'", isBetty ? "" : "NOT ", loggedInUser);

            final String json = getJson(isBetty);
            logger.debug("Responding with the following json: {}", json);
            sendJson(resp, json);
        }

        private void sendJson(HttpServletResponse resp, String json) throws IOException
        {
            resp.setContentType("application/json");
            resp.getWriter().write(json);
            resp.getWriter().close();
        }

        private String getJson(boolean shouldDisplay)
        {
            return "{\"shouldDisplay\" : " + valueOf(shouldDisplay) + "}";
        }

        private boolean isBetty(String loggedInUser)
        {
            return BETTY.equals(loggedInUser);
        }
    }
}
