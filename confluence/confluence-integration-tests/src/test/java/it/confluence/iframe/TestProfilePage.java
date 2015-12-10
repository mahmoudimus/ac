package it.confluence.iframe;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import com.atlassian.connect.test.confluence.pageobjects.ConfluenceUserProfilePage;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ProfilePageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.common.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.TestUser;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.common.matcher.ConnectAsserts.verifyContainsStandardAddOnQueryParamters;
import static com.atlassian.plugin.connect.test.common.matcher.IsNotBlank.isNotBlank;
import static com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet.toggleableConditionBean;
import static com.atlassian.plugin.connect.test.common.util.AddonTestUtils.randomAddOnKey;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of profile page in Confluence
 */
public class TestProfilePage extends ConfluenceWebDriverTestBase
{
    protected static final String MY_AWESOME_PAGE = "My Awesome Page";
    protected static final String MY_AWESOME_PAGE_KEY = "my-awesome-page";

    private static ConnectRunner runner;
    private String addonKey;
    private String awesomePageModuleKey;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn("profilePages", new ProfilePageModuleMeta(), "/my-awesome-profile?profile_user={profileUser.name}&profile_key={profileUser.key}");
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        runner.setToggleableConditionShouldDisplay(true);

        TestUser user = testUserFactory.basicUser();
        login(user);
        ConnectAddOnEmbeddedTestPage page = runCanClickOnPageLinkAndSeeAddonContents(
                LINK_TEXT, MY_AWESOME_PAGE, user);
        Map<String,String> queryParams = page.getIframeQueryParams();
        assertThat(queryParams.get("profile_user"), is(user.getUsername()));
        assertThat(queryParams.get("profile_key"), isNotBlank());
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition()
    {
        runner.setToggleableConditionShouldDisplay(false);

        // web item should not be displayed
        loginAndVisit(testUserFactory.basicUser(), ConfluenceUserProfilePage.class);
        assertThat("Expected web-item for page to NOT be present", connectPageOperations
                .existsWebItem(MY_AWESOME_PAGE_KEY), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class,
                addonKey, MY_AWESOME_PAGE_KEY);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(MY_AWESOME_PAGE));
    }

    @Before
    public void beforeEachTest()
    {
        this.addonKey = runner.getAddon().getKey();
        this.awesomePageModuleKey = addonAndModuleKey(addonKey, MY_AWESOME_PAGE_KEY);
    }


    private ConnectAddOnEmbeddedTestPage runCanClickOnPageLinkAndSeeAddonContents(
            RemoteWebItem.ItemMatchingMode mode, String id, TestUser user)
            throws MalformedURLException, URISyntaxException
    {
        login(user);

        product.visit(ConfluenceUserProfilePage.class);

        LinkedRemoteContent addonPage = connectPageOperations.findConnectPage(mode, id, Optional.<String>empty(),
                awesomePageModuleKey);

        ConnectAddOnEmbeddedTestPage addonContentPage = addonPage.click();

        assertThat(addonContentPage.getMessage(), equalTo("Success"));

        verifyContainsStandardAddOnQueryParamters(addonContentPage.getIframeQueryParams(),
                product.getProductInstance().getContextPath());

        return addonContentPage;
    }

    private static void startConnectAddOn(String fieldName, ConnectModuleMeta meta, String url) throws Exception
    {
        ConnectPageModuleBeanBuilder pageBeanBuilder = new ConnectPageModuleBeanBuilder().withName(new I18nProperty(MY_AWESOME_PAGE, null))
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
}
