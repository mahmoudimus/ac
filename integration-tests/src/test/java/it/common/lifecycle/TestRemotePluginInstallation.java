package it.common.lifecycle;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.GeneralPageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;

public class TestRemotePluginInstallation extends MultiProductWebDriverTestBase
{
    private static final String PAGE_NAME = "Foo";

    @Test
    public void testChangedKey() throws Exception
    {
        login(testUserFactory.basicUser());
        ConnectRunner pluginFirst = createAddOn("pluginFirst");
        assertThatWeCanVisitThePage(pluginFirst);

        ConnectRunner pluginSecond = createAddOn("pluginSecond");
        assertThatWeCanVisitThePage(pluginSecond);
    }

    private void assertThatWeCanVisitThePage(ConnectRunner addOn) throws Exception
    {
        try
        {
            product.visit(HomePage.class);
            product.getPageBinder().bind(GeneralPage.class, "changedPage", addOn.getAddon().getKey()).clickAddOnLink(); // will throw if it fails to load
        }
        finally
        {
            addOn.stopAndUninstall();
        }
    }

    private ConnectRunner createAddOn(String addOnKey) throws Exception
    {
        return new ConnectRunner(product.getProductInstance().getBaseUrl(), addOnKey)
                .addModule("generalPages", newPageBean()
                        .withKey("changedPage")
                        .withName(new I18nProperty("Foo", null))
                        .withUrl("/page")
                        .withLocation(getGloballyVisibleLocation())
                        .build())
                .addModuleMeta(new GeneralPageModuleMeta())
                .addRoute("/page", ConnectAppServlets.helloWorldServlet())
                .setAuthenticationToNone()
                .start();
    }
}
