package it.confluence.iframe;

import java.net.URI;
import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.connect.test.jira.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.jira.pageobjects.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.utils.IframeUtils;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import it.confluence.ConfluenceWebDriverTestBase;
import it.modules.ConnectAsserts;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.servlet.condition.ToggleableConditionServlet;

/**
 * Test of general page in Confluence
 */
public class TestGeneralPage extends ConfluenceWebDriverTestBase
{
    private static final String SPACE = "ds";
    private static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";
    private static final String KEY_MY_CONTEXT_PAGE = "my-context-page";
    private static final String CONTEXT_PAGE_NAME = "My Context Param Page";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();
    private static final String PARAMETER_CAPTURE_CONDITION_URL = "/parameterCapture";
    private static final String PAGE_NAME = "Foo";

    private static ConnectRunner runner;
    private String addonKey;
    private String awesomePageModuleKey;

    @Rule
    public TestRule resetToggleableCondition = runner.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product)
                .setAuthenticationToNone()
                .addModules(
                        "generalPages",
                        ConnectPageModuleBean.newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg?page_id={page.id}&page_version={page.version}&page_type={page.type}")
                                .withWeight(1234)
                                .withLocation("system.header/left")
                                .withConditions(ToggleableConditionServlet.toggleableConditionBean())
                                .build(),
                        ConnectPageModuleBean.newPageBean()
                                .withName(new I18nProperty(CONTEXT_PAGE_NAME, null))
                                .withKey(KEY_MY_CONTEXT_PAGE)
                                .withUrl("/pg?page_id={page.id}")
                                .withWeight(1234)
                                .withLocation("system.header/left")
                                .withConditions(SingleConditionBean.newSingleConditionBean().withCondition(PARAMETER_CAPTURE_CONDITION_URL +
                                        "?page_id={page.id}").build())
                                .build())
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
                .addRoute(PARAMETER_CAPTURE_CONDITION_URL, PARAMETER_CAPTURING_SERVLET)
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
    public void beforeEachTest()
    {
        this.addonKey = runner.getAddon().getKey();
        this.awesomePageModuleKey = ModuleKeyUtils.addonAndModuleKey(addonKey, KEY_MY_AWESOME_PAGE);
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws Exception
    {
        login(testUserFactory.basicUser());

        ConfluenceViewPage createdPage = createAndVisitViewPage();
        ConfluenceGeneralPage generalPage = product.getPageBinder().bind(ConfluenceGeneralPage.class, KEY_MY_AWESOME_PAGE, addonKey);

        URI url = new URI(generalPage.getRemotePluginLinkHref());
        Assert.assertThat(url.getPath(), Matchers.is("/confluence" + IframeUtils.iframeServletPath(addonKey, KEY_MY_AWESOME_PAGE)));

        ConnectAddOnEmbeddedTestPage addonContentsPage = generalPage.clickAddOnLink();

        Assert.assertThat(addonContentsPage.isFullSize(), Matchers.is(true));

        // check iframe url params
        Map<String,String> iframeQueryParams = addonContentsPage.getIframeQueryParams();
        ConnectAsserts.verifyContainsStandardAddOnQueryParamters(iframeQueryParams, product.getProductInstance().getContextPath());
        Assert.assertThat(iframeQueryParams, Matchers.hasEntry("page_id", createdPage.getPageId()));
        Assert.assertThat(iframeQueryParams, Matchers.hasEntry("page_version", "1"));
        Assert.assertThat(iframeQueryParams, Matchers.hasEntry("page_type", "page"));
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition() throws Exception
    {
        runner.setToggleableConditionShouldDisplay(false);

        login(testUserFactory.basicUser());

        // web item should not be displayed
        createAndVisitViewPage();
        Assert.assertThat("Expected web-item for page to NOT be present", connectPageOperations.existsWebItem(awesomePageModuleKey), Matchers.is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, addonKey, ModuleKeyUtils.moduleKeyOnly(awesomePageModuleKey));
        Assert.assertThat(insufficientPermissionsPage.getErrorMessage(), Matchers.containsString("You do not have the correct permissions"));
        Assert.assertThat(insufficientPermissionsPage.getErrorMessage(), Matchers.containsString(PAGE_NAME));
    }

    private ConfluenceViewPage createAndVisitViewPage() throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(Option.some(testUserFactory.basicUser()), SPACE, "A test page", "some page content");
        return product.visit(ConfluenceViewPage.class, pageData.getId());
    }

}
