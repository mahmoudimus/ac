package it.common.jsapi;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.TimeZone;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestRequest extends MultiProductWebDriverTestBase
{

    private static final String PAGE_MODULE_KEY = "remotePluginGeneral";
    private static final String PAGE_NAME = "Request";

    private static ConnectRunner remotePlugin;

    private static TestUser betty;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        betty = testUserFactory.admin();

        String pageUrl = "/rpg";
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT()
                .addModules("generalPages",
                        newPageBean()
                                .withKey(PAGE_MODULE_KEY)
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withUrl(pageUrl)
                                .withLocation(getGloballyVisibleLocation())
                                .build())
                .addRoute(pageUrl, ConnectAppServlets.apRequestServlet())
                .addScope(ScopeName.READ)
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
    public void testRequestFromGeneralPage()
    {
        loginAndVisit(betty, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, PAGE_MODULE_KEY, remotePlugin.getAddon().getKey());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        assertTrue(remotePluginTest.getTitle().contains(PAGE_NAME));
        assertEquals("Success", remotePluginTest.getMessage());
        assertTrue(remotePluginTest.getIframeQueryParams().containsKey("cp"));
        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(betty.getUsername()));
        assertEquals(betty.getUsername(), remotePluginTest.getUserId());
        assertTrue(remotePluginTest.getLocale().startsWith("en-"));

        // timezone should be the same as the default one
        assertEquals(TimeZone.getDefault().getRawOffset(), TimeZone.getTimeZone(remotePluginTest.getTimeZone()).getRawOffset());

        // basic tests of the RA.request API
        assertEquals("200", remotePluginTest.getClientHttpStatus());
        String statusText = remotePluginTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText));
        String contentType = remotePluginTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        assertEquals(betty.getUsername(), remotePluginTest.getClientHttpData());
        assertEquals(betty.getUsername(), remotePluginTest.getClientHttpResponseText());

        // media type tests of the RA.request API
        assertEquals("{\"name\": \"" + betty.getUsername() + "\"}", remotePluginTest.getClientHttpDataJson());
        assertEquals("<user><name>" + betty.getUsername() + "</name></user>", remotePluginTest.getClientHttpDataXml());
    }
}
