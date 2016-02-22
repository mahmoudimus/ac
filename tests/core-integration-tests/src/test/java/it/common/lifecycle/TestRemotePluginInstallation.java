package it.common.lifecycle;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;

import org.junit.Test;

import it.common.MultiProductWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;

public class TestRemotePluginInstallation extends MultiProductWebDriverTestBase {

    @Test
    public void testChangedKey() throws Exception {
        login(testUserFactory.basicUser());
        ConnectRunner pluginFirst = createAddon("pluginFirst");
        assertThatWeCanVisitThePage(pluginFirst);

        ConnectRunner pluginSecond = createAddon("pluginSecond");
        assertThatWeCanVisitThePage(pluginSecond);
    }

    private void assertThatWeCanVisitThePage(ConnectRunner addon) throws Exception {
        try {
            product.visit(HomePage.class);
            product.getPageBinder().bind(GeneralPage.class, "changedPage", addon.getAddon().getKey()).clickAddonLink(); // will throw if it fails to load
        } finally {
            addon.stopAndUninstall();
        }
    }

    private ConnectRunner createAddon(String addonKey) throws Exception {
        return new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .addModule("generalPages", newPageBean()
                        .withKey("changedPage")
                        .withName(new I18nProperty("Foo", null))
                        .withUrl("/page")
                        .withLocation(getGloballyVisibleLocation())
                        .build())
                .addRoute("/page", ConnectAppServlets.helloWorldServlet())
                .setAuthenticationToNone()
                .start();
    }
}
