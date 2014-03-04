package it.capabilities.jira;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static it.capabilities.ConnectAsserts.verifyContainsStandardAddOnQueryParamters;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in JIRA
 */
public class TestGeneralPage extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";
    private static final String PAGE_NAME = "My Awesome Page";

    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("My Awesome Page", null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg?project_id={project.id}&project_key={project.key}")
                                .withConditions(toggleableConditionBean())
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
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
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        loginAsAdmin();

        product.visit(JiraViewProjectPage.class, project.getKey());

        JiraGeneralPage viewProjectPage = product.getPageBinder().bind(JiraGeneralPage.class, KEY_MY_AWESOME_PAGE, PAGE_NAME);

        assertThat(viewProjectPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(viewProjectPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/jira/plugins/servlet/ac/my-plugin/" + KEY_MY_AWESOME_PAGE));

        RemotePluginTestPage addonContentsPage = viewProjectPage.clickRemotePluginLink();
        assertThat(addonContentsPage.isFullSize(), is(true));

        // check iframe url params
        Map<String,String> iframeQueryParams = addonContentsPage.getIframeQueryParams();
        verifyContainsStandardAddOnQueryParamters(iframeQueryParams, product.getProductInstance().getContextPath());
        assertThat(iframeQueryParams, hasEntry("project_key", project.getKey()));
        assertThat(iframeQueryParams, hasEntry("project_id", project.getId()));
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition()
    {
        loginAsAdmin();

        // web item should be displayed
        assertThat("Expected web-item for page to be present", connectPageOperations.existsWebItem(KEY_MY_AWESOME_PAGE), is(true));

        remotePlugin.setToggleableConditionShouldDisplay(false);

        product.visit(HomePage.class);
        // web item should not be displayed
        assertThat("Expected web-item for page to NOT be present", connectPageOperations.existsWebItem(KEY_MY_AWESOME_PAGE), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, PLUGIN_KEY, KEY_MY_AWESOME_PAGE);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(PAGE_NAME));
    }


}
