package it.common.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectGeneralTestPage;
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

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
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
        TestUser user = testUserFactory.basicUser();
        ConnectGeneralTestPage page = loginAndVisit(user,
                ConnectGeneralTestPage.class, remotePlugin.getAddon().getKey(), PAGE_MODULE_KEY);

        assertTrue(page.getTitle().contains(PAGE_NAME));
        assertEquals("Success", page.getMessage());
        assertTrue(page.getIframeQueryParams().containsKey("cp"));
        assertNotNull(page.getFullName());
        assertThat(page.getFullName().toLowerCase(), Matchers.containsString(user.getUsername()));
        assertEquals(user.getUsername(), page.getUserId());
        assertTrue(page.getLocale().startsWith("en-"));

        // timezone should be the same as the default one
        assertEquals(TimeZone.getDefault().getRawOffset(), TimeZone.getTimeZone(page.getTimeZone()).getRawOffset());

        // basic tests of the RA.request API
        assertEquals("200", page.getClientHttpStatus());
        String statusText = page.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText));
        String contentType = page.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        assertEquals(user.getUsername(), page.getClientHttpData());
        assertEquals(user.getUsername(), page.getClientHttpResponseText());

        // media type tests of the RA.request API
        assertEquals("{\"name\": \"" + user.getUsername() + "\"}", page.getClientHttpDataJson());
        assertEquals("<user><name>" + user.getUsername() + "</name></user>", page.getClientHttpDataXml());
    }
}
