package it.modules.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import org.junit.*;
import org.junit.rules.TestRule;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;

import static com.atlassian.fugue.Option.some;
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
 * Test of general page in Confluence
 */
public class TestGeneralPage extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String SPACE = "ds";
    private static final String ADMIN = "admin";
    private static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";
    private static final String KEY_MY_CONTEXT_PAGE = "my-context-page";
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
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("My Awesome Page", null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg?page_id={page.id}&page_version={page.version}&page_type={page.type}")
                                .withWeight(1234)
                                .withConditions(toggleableConditionBean())
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(CONTEXT_PAGE_NAME, null))
                                .withKey(KEY_MY_CONTEXT_PAGE)
                                .withUrl("/pg?page_id={page.id}")
                                .withWeight(1234)
                                .withConditions(newSingleConditionBean().withCondition(PARAMETER_CAPTURE_CONDITION_URL +
                                        "?page_id={page.id}").build())
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
    public void canClickOnPageLinkAndSeeAddonContents() throws Exception
    {
        loginAsAdmin();

        ConfluenceViewPage createdPage = createAndVisitViewPage();
        ConfluenceGeneralPage generalPage = product.getPageBinder().bind(ConfluenceGeneralPage.class, KEY_MY_AWESOME_PAGE, "My Awesome Page", true, addonKey);

        assertThat(generalPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(generalPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/confluence/plugins/servlet/ac/" + addonKey + "/" + KEY_MY_AWESOME_PAGE));

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
        remotePlugin.setToggleableConditionShouldDisplay(false);

        loginAsAdmin();

        // web item should not be displayed
        createAndVisitViewPage();
        assertThat("Expected web-item for page to NOT be present", connectPageOperations.existsWebItem(awesomePageModuleKey), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, addonKey, moduleKeyOnly(awesomePageModuleKey));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("My Awesome Page"));
    }

    @Ignore("need tims fixes")
    @Test
    public void remoteConditionWithParamsIsCorrect() throws Exception
    {
        loginAsAdmin();

        ConfluenceViewPage page = createAndVisitViewPage();
        ConfluenceGeneralPage generalPage = product.getPageBinder().bind(ConfluenceGeneralPage.class, KEY_MY_CONTEXT_PAGE, CONTEXT_PAGE_NAME, true);

        assertThat(generalPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(generalPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/confluence/plugins/servlet/ac/my-plugin/" + KEY_MY_CONTEXT_PAGE));

        ConnectAddOnEmbeddedTestPage addonContentsPage = generalPage.clickAddOnLink();

        assertThat(addonContentsPage.isFullSize(), is(true));

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("page_id"), equalTo(page.getPageId())));
    }
    
    private ConfluenceViewPage createAndVisitViewPage() throws Exception
    {
        return createAndVisitPage(ConfluenceViewPage.class);
    }


    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluenceOps.ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser(ADMIN, ADMIN)), SPACE, "A test page", "some page content");
    }

}
