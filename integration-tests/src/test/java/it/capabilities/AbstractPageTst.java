package it.capabilities;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ToggleableConditionServlet;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static it.servlet.condition.ToggleableConditionServlet.TOGGLE_CONDITION_URL;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AbstractPageTst extends ConnectWebDriverTestBase
{
    protected static final String PLUGIN_KEY = "my-plugin";
    protected static final String MY_AWESOME_PAGE = "My Awesome Page";
    protected static final String MY_AWESOME_PAGE_KEY = "my-awesome-page";
    protected static final String URL = "/" + MY_AWESOME_PAGE_KEY;

    private static ConnectRunner remotePlugin;

    public static final ToggleableConditionServlet TOGGLEABLE_CONDITION_SERVLET = new ToggleableConditionServlet(true);
    @Rule
    public TestRule resetToggleableCondition = TOGGLEABLE_CONDITION_SERVLET.resetToInitialValueRule();

    protected static void startConnectAddOn(String fieldName) throws Exception
    {
        startConnectAddOn(fieldName, newPageBean());
    }

    protected static void startConnectAddOn(String fieldName, ConnectPageModuleBeanBuilder pageBeanBuilder) throws Exception
    {
        pageBeanBuilder.withName(new I18nProperty(MY_AWESOME_PAGE, null))
                .withKey(MY_AWESOME_PAGE_KEY)
                .withUrl(URL)
                .withConditions(
                    newSingleConditionBean().withCondition(TOGGLE_CONDITION_URL).build()
                )
                .withWeight(1234);

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addModule(fieldName, pageBeanBuilder.build())
                .setAuthenticationToNone()
                .addRoute(URL, ConnectAppServlets.apRequestServlet())
                .addRoute(TOGGLE_CONDITION_URL, TOGGLEABLE_CONDITION_SERVLET)
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

    protected <T extends Page> void runCanClickOnPageLinkAndSeeAddonContents(Class<T> pageClass, Option<String> linkText)
            throws MalformedURLException, URISyntaxException
    {
        loginAsAdmin();

        T page = product.visit(pageClass);
        revealLinkIfNecessary(page);

        LinkedRemoteContent addonPage = connectPageOperations.findConnectPage(LINK_TEXT, linkText.getOrElse(MY_AWESOME_PAGE),
                Option.<String>none(), MY_AWESOME_PAGE_KEY);

        RemotePluginEmbeddedTestPage addonContentPage = addonPage.click();

        assertThat(addonContentPage.isLoaded(), equalTo(true));
        assertThat(addonContentPage.getMessage(), equalTo("Success"));
    }

    protected <T extends Page> void revealLinkIfNecessary(T page)
    {
    }


}
