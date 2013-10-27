package it.capabilities.jira;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
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

import it.HttpContextServlet;
import it.MyContextAwareWebPanelServlet;
import it.capabilities.CheckUsernameConditionServlet;
import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;
import static java.lang.String.valueOf;
import static org.junit.Assert.*;
import static it.capabilities.ConnectAsserts.*;


/**
 * @since 1.0
 */
public class TestJiraWebItem extends JiraWebDriverTestBase
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
                        .withLink("/irwi?issue_id=${issue.id}&project_key=${project.key}&pid=${project.id}")
                        .build())
                .addCapability(newWebItemBean()
                        .withContext(AddOnUrlContext.product)
                        .withName(new I18nProperty("Quick project link", "ac.qp"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("/browse/ACDEV-1234")
                        .build())
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("google link", "ac.gl"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build()
                                ,newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                        )
                        .build())
                
                .addRoute("/onlyBarneyCondition", new CheckUsernameConditionServlet(BARNEY_USERNAME))
                .addRoute("/onlyBettyCondition", new CheckUsernameConditionServlet(BETTY_USERNAME))
                .addRoute("/irwi?issue_id=${issue.id}&project_key=${project.key}&pid=${project.id}", new HttpContextServlet(new MyContextAwareWebPanelServlet()))
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
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ABSOLUTE_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        assertTrue("Web item link should be absolute", webItem.isPonitingToOldXmlInternalUrl());
        assertURIEquals("http://www.google.com", webItem.getPath());
    }
    
    //TODO: add these tests back in once url variable substitution is working for web items
//    @Test
//    public void testRelativeWebItem()
//    {
//        loginAsAdmin();
//
//        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
//        RemoteWebItem webItem = viewProjectPage.findWebItem(ADDON_WEBITEM, Optional.<String>absent());
//        assertNotNull("Web item should be found", webItem);
//        
//        assertEquals(project.getKey(), webItem.getFromQueryString("project_key"));
//        assertEquals(project.getId(), webItem.getFromQueryString("pid"));
//    }
//
//    @Test
//    public void testProductWebItem()
//    {
//        loginAsAdmin();
//
//        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
//        RemoteWebItem webItem = viewProjectPage.findWebItem(PRODUCT_WEBITEM, Optional.<String>absent());
//        assertNotNull("Web item should be found", webItem);
//
//        webItem.click();
//
//        assertFalse("Web item link shouldn't be absolute", webItem.isPonitingToOldXmlInternalUrl());
//        assertThat(webItem.getPath(), endsWith(project.getKey()));
//    }

    @Test
    public void bettyCanSeeWebItem()
    {
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ABSOLUTE_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(ABSOLUTE_WEBITEM));
    }

    //TODO: once generalPage is complete, add a test to check that a web item pointing to the page works properly

        
}
