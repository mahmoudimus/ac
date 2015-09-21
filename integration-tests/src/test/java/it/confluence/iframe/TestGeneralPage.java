package it.confluence.iframe;

import com.atlassian.plugin.connect.modules.beans.GeneralPageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.URI;
import java.util.Map;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;
import static it.modules.ConnectAsserts.verifyContainsStandardAddOnQueryParamters;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg?page_id={page.id}&page_version={page.version}&page_type={page.type}")
                                .withWeight(1234)
                                .withLocation("system.header/left")
                                .withConditions(toggleableConditionBean())
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(CONTEXT_PAGE_NAME, null))
                                .withKey(KEY_MY_CONTEXT_PAGE)
                                .withUrl("/pg?page_id={page.id}")
                                .withWeight(1234)
                                .withLocation("system.header/left")
                                .withConditions(newSingleConditionBean().withCondition(PARAMETER_CAPTURE_CONDITION_URL +
                                        "?page_id={page.id}").build())
                                .build())
                .addModuleMeta(new GeneralPageModuleMeta())
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
        this.awesomePageModuleKey = addonAndModuleKey(addonKey, KEY_MY_AWESOME_PAGE);
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws Exception
    {
        login(testUserFactory.basicUser());

        ConfluenceViewPage createdPage = createAndVisitViewPage();
        ConfluenceGeneralPage generalPage = product.getPageBinder().bind(ConfluenceGeneralPage.class, KEY_MY_AWESOME_PAGE, addonKey);

        URI url = new URI(generalPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/confluence" + IframeUtils.iframeServletPath(addonKey, KEY_MY_AWESOME_PAGE)));

        ConnectAddOnEmbeddedTestPage addonContentsPage = generalPage.clickAddOnLink();

        assertThat(addonContentsPage.isFullSize(), is(true));

        // check iframe url params
        Map<String,String> iframeQueryParams = addonContentsPage.getIframeQueryParams();
        verifyContainsStandardAddOnQueryParamters(iframeQueryParams, product.getProductInstance().getContextPath());
        assertThat(iframeQueryParams, hasEntry("page_id", createdPage.getPageId()));
        assertThat(iframeQueryParams, hasEntry("page_version", "1"));
        assertThat(iframeQueryParams, hasEntry("page_type", "page"));
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition() throws Exception
    {
        runner.setToggleableConditionShouldDisplay(false);

        login(testUserFactory.basicUser());

        // web item should not be displayed
        createAndVisitViewPage();
        assertThat("Expected web-item for page to NOT be present", connectPageOperations.existsWebItem(awesomePageModuleKey), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, addonKey, moduleKeyOnly(awesomePageModuleKey));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(PAGE_NAME));
    }

    private ConfluenceViewPage createAndVisitViewPage() throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(testUserFactory.basicUser()), SPACE, "A test page", "some page content");
        return product.visit(ConfluenceViewPage.class, pageData.getId());
    }

}
