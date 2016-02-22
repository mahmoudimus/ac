package it.jira.iframe;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.atlassian.connect.test.jira.pageobjects.JiraViewProjectPage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingConditionServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import com.atlassian.plugin.connect.test.jira.pageobjects.JiraGeneralPage;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;
import static com.atlassian.plugin.connect.test.common.matcher.ConnectAsserts.verifyContainsStandardAddonQueryParameters;
import static com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in JIRA
 */
public class TestGeneralPage extends JiraWebDriverTestBase {
    private static final String KEY_MY_CONTEXT_PAGE = "my-context-page";
    private static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";
    private static final String PAGE_NAME = "My Awesome Page";
    private static final String CONTEXT_PAGE_NAME = "My Context Param Page";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();
    private static final String PARAMETER_CAPTURE_CONDITION_URL = "/parameterCapture";

    private static ConnectRunner remotePlugin;

    private static String addonKey;
    private String awesomePageModuleKey;
    private String contextPageModuleKey;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        addonKey = AddonTestUtils.randomAddonKey();
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .setAuthenticationToNone()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg?project_id={project.id}&project_key={project.key}")
                                .withConditions(toggleableConditionBean())
                                .withWeight(1234)
                                .build()
                        , newPageBean()
                                .withName(new I18nProperty(CONTEXT_PAGE_NAME, null))
                                .withKey(KEY_MY_CONTEXT_PAGE)
                                .withUrl("/pg?project_id={project.id}&project_key={project.key}")
                                .withConditions(newSingleConditionBean().withCondition(PARAMETER_CAPTURE_CONDITION_URL +
                                        "?project_id={project.id}&project_key={project.key}").build())
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
                .addRoute(PARAMETER_CAPTURE_CONDITION_URL, PARAMETER_CAPTURING_SERVLET)
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void beforeEachTest() {
        this.awesomePageModuleKey = addonAndModuleKey(addonKey, KEY_MY_AWESOME_PAGE);
        this.contextPageModuleKey = addonAndModuleKey(addonKey, KEY_MY_CONTEXT_PAGE);
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException {
        loginAndVisit(testUserFactory.basicUser(), JiraViewProjectPage.class, project.getKey());

        JiraGeneralPage viewProjectPage = product.getPageBinder().bind(JiraGeneralPage.class, KEY_MY_AWESOME_PAGE, addonKey);

        URI url = new URI(viewProjectPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/jira" + IframeUtils.iframeServletPath(addonKey, KEY_MY_AWESOME_PAGE)));

        ConnectAddonEmbeddedTestPage addonContentsPage = viewProjectPage.clickAddonLink();
        assertThat(addonContentsPage.isFullSize(), is(true));

        // check iframe url params
        Map<String, String> iframeQueryParams = addonContentsPage.getIframeQueryParams();
        verifyContainsStandardAddonQueryParameters(iframeQueryParams, product.getProductInstance().getContextPath());
        assertThat(iframeQueryParams, hasEntry("project_key", project.getKey()));
        assertThat(iframeQueryParams, hasEntry("project_id", project.getId()));
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition() {
        loginAndVisit(testUserFactory.basicUser(), HomePage.class);

        // web item should be displayed
        assertThat("Expected web-item for page to be present", connectPageOperations.existsWebItem(awesomePageModuleKey), is(true));

        remotePlugin.setToggleableConditionShouldDisplay(false);

        product.visit(HomePage.class);
        // web item should not be displayed
        assertThat("Expected web-item for page to NOT be present", connectPageOperations.existsWebItem(awesomePageModuleKey), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, addonKey, moduleKeyOnly(awesomePageModuleKey));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(PAGE_NAME));
    }
}
