package it.jira.iframe;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.util.ConnectTestUserFactory;
import it.util.TestUser;
import org.junit.*;
import org.junit.rules.TestRule;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;
import static it.modules.ConnectAsserts.verifyContainsStandardAddOnQueryParamters;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in JIRA
 */
public class TestGeneralPage extends JiraWebDriverTestBase
{
    private static final String KEY_MY_CONTEXT_PAGE = "my-context-page";
    private static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";
    private static final String PAGE_NAME = "My Awesome Page";
    private static final String CONTEXT_PAGE_NAME = "My Context Param Page";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();
    private static final String PARAMETER_CAPTURE_CONDITION_URL = "/parameterCapture";
    
    private static ConnectRunner remotePlugin;
    
    private String addonKey;
    private String awesomePageModuleKey;
    private String contextPageModuleKey;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
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
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void beforeEachTest()
    {
        this.addonKey = remotePlugin.getAddon().getKey();
        this.awesomePageModuleKey = addonAndModuleKey(addonKey,KEY_MY_AWESOME_PAGE);
        this.contextPageModuleKey = addonAndModuleKey(addonKey,KEY_MY_CONTEXT_PAGE);
    }
    
    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        loginAndVisit(testUserFactory.basicUser(), JiraViewProjectPage.class, project.getKey());

        JiraGeneralPage viewProjectPage = product.getPageBinder().bind(JiraGeneralPage.class, KEY_MY_AWESOME_PAGE, PAGE_NAME, addonKey);

        URI url = new URI(viewProjectPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/jira/plugins/servlet/ac/" + addonKey + "/" + KEY_MY_AWESOME_PAGE));

        ConnectAddOnEmbeddedTestPage addonContentsPage = viewProjectPage.clickAddOnLink();
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

    @Ignore("figure out why the context is being set")
    @Test
    public void remoteConditionWithParamsIsCorrect() throws Exception
    {
        loginAndVisit(testUserFactory.basicUser(), HomePage.class);

        remotePlugin.setToggleableConditionShouldDisplay(false);
        
        product.visit(JiraViewProjectPage.class, project.getKey());

        JiraGeneralPage viewProjectPage = product.getPageBinder().bind(JiraGeneralPage.class, contextPageModuleKey, CONTEXT_PAGE_NAME);

        URI url = new URI(viewProjectPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/jira/plugins/servlet/ac/my-plugin/" + KEY_MY_CONTEXT_PAGE));

        PARAMETER_CAPTURING_SERVLET.clearParams();

        PARAMETER_CAPTURING_SERVLET.setConditionReturnValue("false");
        
        viewProjectPage.clickRemotePluginLinkWithoutBinding();

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("project_key"), equalTo(project.getKey())));
    }
}
