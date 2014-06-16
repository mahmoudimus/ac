package it.capabilities;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AbstractPageTestBase extends ConnectWebDriverTestBase
{
    protected static final String MY_AWESOME_PAGE = "My Awesome Page";
    protected static final String MY_AWESOME_PAGE_KEY = "my-awesome-page";
    protected static final String URL = "/" + MY_AWESOME_PAGE_KEY;

    protected static ConnectRunner remotePlugin;
    
    protected String pluginKey;
    protected String awesomePageModuleKey;
    

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

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

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), RemotePluginUtils.randomPluginKey())
                .addModule(fieldName, pageBeanBuilder.build())
                .setAuthenticationToNone()
                .addRoute(route, ConnectAppServlets.apRequestServlet())
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
    
    @Before
    public void beforeEachTestBase()
    {
        this.pluginKey = remotePlugin.getAddon().getKey();
        this.awesomePageModuleKey = addonAndModuleKey(pluginKey,MY_AWESOME_PAGE_KEY);
    }

    protected <T extends Page> RemotePluginEmbeddedTestPage runCanClickOnPageLinkAndSeeAddonContents(Class<T> pageClass, Option<String> linkText)
            throws MalformedURLException, URISyntaxException
    {
        
        loginAsAdmin();

        T page = product.visit(pageClass);
        revealLinkIfNecessary(page);

        LinkedRemoteContent addonPage = connectPageOperations.findConnectPage(LINK_TEXT, linkText.getOrElse(MY_AWESOME_PAGE),
                Option.<String>none(), awesomePageModuleKey);

        RemotePluginEmbeddedTestPage addonContentPage = addonPage.click();

        assertThat(addonContentPage.isLoaded(), equalTo(true));
        assertThat(addonContentPage.getMessage(), equalTo("Success"));

        ConnectAsserts.verifyContainsStandardAddOnQueryParamters(addonContentPage.getIframeQueryParams(),
                product.getProductInstance().getContextPath());

        return addonContentPage;
    }

    protected <T extends Page> void revealLinkIfNecessary(T page)
    {
    }


}
