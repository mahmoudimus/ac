package it.confluence.iframe;

import java.net.URI;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceAdminPage;
import com.atlassian.connect.test.confluence.pageobjects.ConnectConfluenceAdminHomePage;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;

import com.google.common.base.Supplier;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import it.confluence.ConfluenceWebDriverTestBase;

/**
 * Test of general page in Confluence
 */
public class TestAdminPage extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();

    private static final String PAGE_NAME = "My Admin Page";
    private static final String PAGE_KEY = "my-admin-page";

    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "adminPages",
                        ConnectPageModuleBean.newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(PAGE_KEY)
                                .withUrl("/pg")
                                .withWeight(1234)
                                .withConditions(ToggleableConditionServlet.toggleableConditionBean())
                                .build())
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet())
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
    public void canClickOnPageLinkAndSeeAddonContents() throws Exception
    {
        loginAndVisit(testUserFactory.admin(), ConfluenceAdminHomePage.class);

        ConfluenceAdminPage adminPage = product.getPageBinder().bind(ConfluenceAdminPage.class, PLUGIN_KEY, PAGE_KEY);

        URI url = new URI(adminPage.getRemotePluginLinkHref());
        Assert.assertThat(url.getPath(), Matchers.is("/confluence" + IframeUtils.iframeServletPath(PLUGIN_KEY, PAGE_KEY)));

        // TODO Admin page web-item location has incorrect text ("OSGi")

        final ConnectAddOnEmbeddedTestPage addonContentsPage = adminPage.clickAddOnLink();
        Poller.waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return "Hello world".equals(addonContentsPage.getValueById("hello-world-message"));
            }
        }));
    }

    @Test
    public void nonAdminCanNotSeePage()
    {
        login(testUserFactory.basicUser());
        InsufficientPermissionsPage page = product.visit(InsufficientPermissionsPage.class, PLUGIN_KEY, PAGE_KEY);
        Assert.assertThat(page.getErrorMessage(), Matchers.containsString("You do not have the correct permissions"));
        Assert.assertThat(page.getErrorMessage(), Matchers.containsString("My Admin Page"));
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition()
    {
        remotePlugin.setToggleableConditionShouldDisplay(false);

        // web item should not be displayed
        ConnectConfluenceAdminHomePage adminPage = loginAndVisit(testUserFactory.admin(), ConnectConfluenceAdminHomePage.class);
        Assert.assertThat("Expected web-item for page to NOT be present", adminPage.getWebItem(PAGE_KEY).isPresent(), Matchers.is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, PLUGIN_KEY, PAGE_KEY);
        Assert.assertThat(insufficientPermissionsPage.getErrorMessage(), Matchers.containsString("You do not have the correct permissions"));
        Assert.assertThat(insufficientPermissionsPage.getErrorMessage(), Matchers.containsString("My Admin Page"));
    }


}
