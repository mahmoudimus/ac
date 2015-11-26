package it.common.iframe;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.TestUser;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;

import it.common.MultiProductWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.common.util.AddonTestUtils.randomAddOnKey;
import static com.atlassian.plugin.connect.test.common.matcher.ConnectAsserts.verifyContainsStandardAddOnQueryParamters;
import static com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AbstractPageTestBase<T extends Page> extends MultiProductWebDriverTestBase
{
    protected static final String MY_AWESOME_PAGE = "My Awesome Page";
    protected static final String MY_AWESOME_PAGE_KEY = "my-awesome-page";
    protected static final String URL = "/" + MY_AWESOME_PAGE_KEY;

    protected static ConnectRunner runner;

    protected String addonKey;
    protected String awesomePageModuleKey;

    @Rule
    public TestRule resetToggleableCondition = runner.resetToggleableConditionRule();

    protected static void startConnectAddOn(String fieldName, ConnectModuleMeta meta) throws Exception
    {
        startConnectAddOn(fieldName, meta, URL);
    }

    protected static void startConnectAddOn(String fieldName, ConnectModuleMeta meta, String url) throws Exception
    {
        startConnectAddOn(fieldName, meta, url, newPageBean());
    }

    protected static void startConnectAddOn(String fieldName, ConnectModuleMeta meta, String url, ConnectPageModuleBeanBuilder pageBeanBuilder) throws Exception
    {
        pageBeanBuilder.withName(new I18nProperty(MY_AWESOME_PAGE, null))
                .withKey(MY_AWESOME_PAGE_KEY)
                .withUrl(url)
                .withConditions(toggleableConditionBean())
                .withWeight(1234);

        int query = url.indexOf("?");
        String route = query > -1 ? url.substring(0, query) : url;

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), randomAddOnKey())
                .addModule(fieldName, pageBeanBuilder.build())
                .addModuleMeta(meta)
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
        this.addonKey = runner.getAddon().getKey();
        this.awesomePageModuleKey = addonAndModuleKey(addonKey, MY_AWESOME_PAGE_KEY);
    }

    protected ConnectAddOnEmbeddedTestPage runCanClickOnPageLinkAndSeeAddonContents(Class<T> pageClass,
            RemoteWebItem.ItemMatchingMode mode, String id, TestUser user)
            throws MalformedURLException, URISyntaxException
    {
        login(user);

        T page = product.visit(pageClass);
        revealLinkIfNecessary(page);

        LinkedRemoteContent addonPage = connectPageOperations().findConnectPage(mode, id, Option.<String>none(),
                awesomePageModuleKey);

        ConnectAddOnEmbeddedTestPage addonContentPage = addonPage.click();

        assertThat(addonContentPage.getMessage(), equalTo("Success"));

        verifyContainsStandardAddOnQueryParamters(addonContentPage.getIframeQueryParams(),
                product.getProductInstance().getContextPath());

        return addonContentPage;
    }

    protected void revealLinkIfNecessary(T page)
    {
    }
}
