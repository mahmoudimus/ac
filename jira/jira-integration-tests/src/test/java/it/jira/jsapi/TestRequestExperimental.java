package it.jira.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.jira.pageobjects.JiraRequestExperimentalTestPage;
import it.jira.JiraWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRequestExperimental extends JiraWebDriverTestBase {
    private static final String PAGE_MODULE_KEY = "remotePluginGeneral";
    private static final String PAGE_NAME = "Request";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        String pageUrl = "/rpg";
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .addJWT()
                .addModules("generalPages",
                        newPageBean()
                                .withKey(PAGE_MODULE_KEY)
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withUrl(pageUrl)
                                .build())
                .addRoute(pageUrl, ConnectAppServlets.apRequestExperimentalServlet())
                .addScope(ScopeName.READ)
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testRequestExperimental() throws Exception {
        TestUser user = testUserFactory.basicUser();
        JiraRequestExperimentalTestPage page = loginAndVisit(user,
                JiraRequestExperimentalTestPage.class, remotePlugin.getAddon().getKey(), PAGE_MODULE_KEY);

        assertEquals("412", page.getIndexedClientHttpStatus(1));
        String statusText1 = page.getIndexedClientHttpStatusText(1);
        assertTrue("Precondition Failed".equals(statusText1));
        assertEquals("Experimental header missing.", page.getIndexedClientHttpResponseText(1));

        // this one has the experimental flag set to true
        assertEquals("200", page.getIndexedClientHttpStatus(2));
        String statusText2 = page.getIndexedClientHttpStatusText(2);
        assertTrue("OK".equals(statusText2) || "success".equals(statusText2));
        assertEquals("{\"name\": \"" + user.getUsername() + "\"}", page.getIndexedClientHttpResponseText(2));

        assertEquals("412", page.getIndexedClientHttpStatus(3));
        String statusText3 = page.getIndexedClientHttpStatusText(3);
        assertTrue("Precondition Failed".equals(statusText3));
        assertEquals("Experimental header missing.", page.getIndexedClientHttpResponseText(3));

        assertEquals("412", page.getIndexedClientHttpStatus(4));
        String statusText4 = page.getIndexedClientHttpStatusText(4);
        assertTrue("Precondition Failed".equals(statusText4));
        assertEquals("Experimental header missing.", page.getIndexedClientHttpResponseText(4));

        assertEquals("412", page.getIndexedClientHttpStatus(5));
        String statusText5 = page.getIndexedClientHttpStatusText(5);
        assertTrue("Precondition Failed".equals(statusText5));
        assertEquals("Experimental header missing.", page.getIndexedClientHttpResponseText(5));

        assertEquals("412", page.getIndexedClientHttpStatus(6));
        String statusText6 = page.getIndexedClientHttpStatusText(6);
        assertTrue("Precondition Failed".equals(statusText6));
        assertEquals("Experimental header missing.", page.getIndexedClientHttpResponseText(6));
    }

}
