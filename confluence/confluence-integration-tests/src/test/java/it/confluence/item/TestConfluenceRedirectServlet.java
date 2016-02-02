package it.confluence.item;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceViewPage;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonHelloWorldPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePageUtil;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestConfluenceRedirectServlet extends ConfluenceWebDriverTestBase
{
    private static final String ADDON_WEBITEM = "ac-general-web-item";
    private static final String ADDON_WEBITEM_FOR_LOGGED_USERS = "ac-general-web-item-for-logged";
    private static final InstallHandlerServlet INSTALL_HANDLER_SERVLET = ConnectAppServlets.installHandlerServlet();
    private static final String SPACE = "ds";
    private static final String WEB_ITEM_ON_URL = "/irwi";
    private static final ParameterCapturingServlet PARAMETER_CAPTURING_DIRECT_WEBITEM_SERVLET = ConnectAppServlets.parameterCapturingServlet(ConnectAppServlets.simplePageServlet());

    private final String baseUrl = product.getProductInstance().getBaseUrl();
    private final String addOnKey = AddonTestUtils.randomAddonKey();
    private ConnectRunner runner;

    @BeforeClass
    public static void setupUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(false);
    }

    @Before
    public void setUp() throws Exception
    {
        WebItemTargetBean pageTarget = newWebItemTargetBean()
                .withType(WebItemTargetType.page)
                .build();

        runner = new ConnectRunner(baseUrl, addOnKey)
                .addJWT(INSTALL_HANDLER_SERVLET)
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("Web Item", null))
                                .withKey(ADDON_WEBITEM)
                                .withTarget(pageTarget)
                                .withLocation("system.content.action")
                                .withUrl(WEB_ITEM_ON_URL + "?always_allowed_param={page.type}&restricted_param={space.key}")
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Only for logged", null))
                                .withKey(ADDON_WEBITEM_FOR_LOGGED_USERS)
                                .withLocation("system.content.action")
                                .withUrl(WEB_ITEM_ON_URL)
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_logged_in").build()
                                ).build()
                )
                .addRoute(WEB_ITEM_ON_URL, ConnectAppServlets.wrapContextAwareServlet(PARAMETER_CAPTURING_DIRECT_WEBITEM_SERVLET))
                .start();
    }

    @After
    public void tearDown() throws Exception
    {
        runner.stopAndUninstall();
    }

    @AfterClass
    public static void tearDownUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(true);
    }

    @Test
    public void shouldResolveUriParamsForRedirection() throws Exception
    {
        login(testUserFactory.basicUser());

        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(ADDON_WEBITEM));
        assertNotNull("Web item should be found", webItem);

        webItem.click();
        product.getPageBinder().bind(ConnectAddonHelloWorldPage.class);

        Map<String, String> queryParams = PARAMETER_CAPTURING_DIRECT_WEBITEM_SERVLET.getParamsFromLastRequest();
        assertThat(queryParams.get("always_allowed_param"), is("page"));
        assertThat(queryParams.get("restricted_param"), is(SPACE));
    }

    @Test
    public void shouldFilterOutUriParamsForWhichUserDoesNotHavePermission() throws IOException
    {
        URI redirectUrl = UriBuilder.fromPath(baseUrl)
                .path(RedirectServletPath.forModule(addOnKey, ADDON_WEBITEM))
                .queryParam("page.type", "page")
                .queryParam("space.key", SPACE)
                .build();

        HttpURLConnection response = doRedirectRequest(redirectUrl);
        String urlToAddOn = response.getHeaderField("Location");
        assertThat(getQueryParam("always_allowed_param", urlToAddOn), is("page"));
        assertThat(getQueryParam("restricted_param", urlToAddOn), isEmptyOrNullString());
    }

    @Test
    public void shouldReturnNotFoundWhenConditionEvaluatesToFalse() throws Exception
    {
        URI redirectUrl = UriBuilder.fromPath(baseUrl)
                .path(RedirectServletPath.forModule(addOnKey, ADDON_WEBITEM_FOR_LOGGED_USERS))
                .build();

        HttpURLConnection response = doRedirectRequest(redirectUrl);
        assertThat(response.getResponseCode(), Matchers.is(HttpStatus.SC_NOT_FOUND));
    }

    private RemoteWebItem findViewPageWebItem(String moduleKey) throws Exception
    {
        ConfluenceOps.ConfluencePageData page = confluenceOps.setPage(some(testUserFactory.admin()), SPACE, "Page with webitem", "some page content");
        product.visit(ConfluenceViewPage.class, page.getId());
        return confluencePageOperations.findWebItem(moduleKey, Optional.<String>empty());
    }

    private String getModuleKey(String module)
    {
        return addonAndModuleKey(runner.getAddon().getKey(), module);
    }

    private HttpURLConnection doRedirectRequest(URI uri) throws IOException
    {
        return (HttpURLConnection) uri.toURL().openConnection();
    }

    private String getQueryParam(String key, String location)
    {
        return RemotePageUtil.findInContext(location, key);
    }
}
