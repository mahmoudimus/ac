package it.common.iframe;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.helptips.HelpTipApiClient;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import it.common.MultiProductWebDriverTestBase;
import it.modules.ConnectAsserts;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AbstractPageTestBase extends MultiProductWebDriverTestBase
{
    protected static final String MY_AWESOME_PAGE = "My Awesome Page";
    protected static final String MY_AWESOME_PAGE_KEY = "my-awesome-page";
    protected static final String URL = "/" + MY_AWESOME_PAGE_KEY;

    protected static ConnectRunner runner;

    protected String pluginKey;
    protected String awesomePageModuleKey;


    @Rule
    public TestRule resetToggleableCondition = runner.resetToggleableConditionRule();

    protected static void startConnectAddOn(String fieldName) throws Exception
    {
        startConnectAddOn(fieldName, URL);
    }

    protected static void startConnectAddOn(String fieldName, String url) throws Exception
    {
        startConnectAddOn(fieldName, url, newPageBean());
    }

    protected static void startConnectAddOn(String fieldName, String url, ConnectPageModuleBeanBuilder pageBeanBuilder) throws Exception
    {
        pageBeanBuilder.withName(new I18nProperty(MY_AWESOME_PAGE, null))
                .withKey(MY_AWESOME_PAGE_KEY)
                .withUrl(url)
                .withConditions(toggleableConditionBean())
                .withWeight(1234);

        int query = url.indexOf("?");
        String route = query > -1 ? url.substring(0, query) : url;

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addModule(fieldName, pageBeanBuilder.build())
                .setAuthenticationToNone()
                .addRoute(route, ConnectAppServlets.apRequestServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Before
    public void beforeEachTestBase()
    {
        this.pluginKey = runner.getAddon().getKey();
        this.awesomePageModuleKey = addonAndModuleKey(pluginKey, MY_AWESOME_PAGE_KEY);
    }

    protected <T extends Page> ConnectAddOnEmbeddedTestPage runCanClickOnPageLinkAndSeeAddonContents(Class<T> pageClass, Option<String> linkText)
            throws MalformedURLException, URISyntaxException
    {
        login(TestUser.ADMIN);

        T page = product.visit(pageClass);
        revealLinkIfNecessary(page);

        LinkedRemoteContent addonPage = connectPageOperations.findConnectPage(LINK_TEXT, linkText.getOrElse(MY_AWESOME_PAGE),
                Option.<String>none(), awesomePageModuleKey);

        ConnectAddOnEmbeddedTestPage addonContentPage = addonPage.click();

        assertThat(addonContentPage.getMessage(), equalTo("Success"));

        ConnectAsserts.verifyContainsStandardAddOnQueryParamters(addonContentPage.getIframeQueryParams(),
                product.getProductInstance().getContextPath());

        return addonContentPage;
    }

    protected <T extends Page> void revealLinkIfNecessary(T page)
    {
    }


}
