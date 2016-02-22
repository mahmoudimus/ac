package it.common.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteMessageGeneralPage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.common.MultiProductWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestMessage extends MultiProductWebDriverTestBase {
    private static final String PAGE_KEY = "ac-message-general-page";

    private static ConnectRunner remotePlugin;


    @BeforeClass
    public static void startConnectAddon() throws Exception {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Msg", null))
                                .withUrl("/pg")
                                .withKey(PAGE_KEY)
                                .withLocation(getGloballyVisibleLocation())
                                .build()
                )
                .addRoute("/pg", ConnectAppServlets.openMessageServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    /**
     * Tests opening an info message from a general page with json descriptor
     */
    @Test
    public void testCreateInfoMessage() throws Exception {
        RemoteMessageGeneralPage page = loginAndVisit(testUserFactory.basicUser(),
                RemoteMessageGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        page.openInfoMessage();
        assertEquals(page.getMessageTitleText(), "plain text title");
    }
}

