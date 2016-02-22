package it.common.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectGeneralTestPage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.common.MultiProductWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestAmd extends MultiProductWebDriverTestBase {

    private static final String PAGE_KEY = "amdTest";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .addJWT()
                .addModules("generalPages",
                        newPageBean()
                                .withKey(PAGE_KEY)
                                .withName(new I18nProperty("AMD", null))
                                .withUrl("/amdTest")
                                .withLocation(getGloballyVisibleLocation())
                                .build())
                .addRoute("/amdTest", ConnectAppServlets.amdTestServlet())
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
    public void testAmd() {
        ConnectGeneralTestPage page = loginAndVisit(testUserFactory.basicUser(),
                ConnectGeneralTestPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        assertEquals("true", page.waitForValue("amd-env"));
        assertEquals("true", page.waitForValue("amd-request"));
        assertEquals("true", page.waitForValue("amd-dialog"));
    }
}
