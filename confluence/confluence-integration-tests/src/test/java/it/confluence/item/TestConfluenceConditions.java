package it.confluence.item;

import java.util.Map;

import com.atlassian.connect.test.confluence.pageobjects.ConfluenceEditPage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.api.request.HttpHeaderNames;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import com.google.common.base.Optional;

import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.IsEqual;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;
import it.matcher.IsLong;
import it.matcher.ParamMatchers;
import it.servlet.condition.CheckUsernameConditionServlet;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.util.TestUser;

public class TestConfluenceConditions extends ConfluenceWebDriverTestBase
{
    private static ConnectRunner remotePlugin;

    private static String onlyBettyWebItem;
    private static String bettyAndBarneyWebItem;
    private static final String ADMIN_RIGHTS_WEBITEM = "admin-rights";
    private static final String CONTEXT_PARAMETERIZED_WEBITEM = "context-parameterized";
    private static final String SPACE_CONTEXT_PARAMETERIZED_WEB_PANEL = CONTEXT_PARAMETERIZED_WEBITEM + "-space";

    private static String onlyBettyConditionUrl;
    private static String onlyBarneyConditionUrl;
    private static final String PARAMETER_CAPTURE_CONDITION_URL = "/parameterCapture";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();
    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET2 = new ParameterCapturingConditionServlet(); // a 2nd to receive condition requests on the same page

    private static TestUser betty;
    private static TestUser barney;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        betty = testUserFactory.admin();
        barney = testUserFactory.basicUser();

        onlyBettyWebItem = "only-" + betty.getDisplayName();
        bettyAndBarneyWebItem = betty.getDisplayName() + "-and-" + barney.getDisplayName();
        onlyBettyConditionUrl = "/only" + betty.getDisplayName() + "Condition";
        onlyBarneyConditionUrl = "/only" + barney.getDisplayName() + "Condition";

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty("Only Betty", onlyBettyWebItem))
                                .withKey(onlyBettyWebItem)
                                .withLocation("system.browse")
                                .withWeight(1)
                                .withUrl("http://www.google.com")
                                .withConditions(
                                        SingleConditionBean.newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                        SingleConditionBean.newSingleConditionBean().withCondition(onlyBettyConditionUrl).build()
                                )
                                .build(),
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty("Betty And Barney", bettyAndBarneyWebItem))
                                .withKey(bettyAndBarneyWebItem)
                                .withLocation("system.browse")
                                .withWeight(1)
                                .withUrl("http://www.google.com")
                                .withConditions(
                                        SingleConditionBean.newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                        CompositeConditionBean.newCompositeConditionBean()
                                                .withType(CompositeConditionType.OR)
                                                .withConditions(
                                                        SingleConditionBean.newSingleConditionBean().withCondition(onlyBettyConditionUrl).build(),
                                                        SingleConditionBean.newSingleConditionBean().withCondition(onlyBarneyConditionUrl).build()
                                                ).build()
                                ).build(),
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty("Admin Rights", ADMIN_RIGHTS_WEBITEM))
                                .withKey(ADMIN_RIGHTS_WEBITEM)
                                .withLocation("system.browse")
                                .withWeight(1)
                                .withUrl("http://www.google.com")
                                .withConditions(
                                        SingleConditionBean.newSingleConditionBean().withCondition("user_is_confluence_administrator").build()
                                )
                                .build(),
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty("Context Parameterized", CONTEXT_PARAMETERIZED_WEBITEM))
                                .withKey(CONTEXT_PARAMETERIZED_WEBITEM)
                                .withLocation("system.browse")
                                .withContext(AddOnUrlContext.addon)
                                .withWeight(1)
                                .withUrl("/somewhere")
                                .withConditions(
                                        SingleConditionBean.newSingleConditionBean().withCondition(PARAMETER_CAPTURE_CONDITION_URL +
                                                "?pageId={page.id}&spaceKey={space.key}").build()
                                )
                                .build())
                .addModules("webPanels",
                        WebPanelModuleBean.newWebPanelBean()
                                .withName(new I18nProperty("Space Context Parameterized", SPACE_CONTEXT_PARAMETERIZED_WEB_PANEL))
                                .withKey(SPACE_CONTEXT_PARAMETERIZED_WEB_PANEL)
                                .withLocation("atl.general") // this location needs testing for space params; see AC-1018
                                .withUrl("/somewhere-else")
                                .withConditions(
                                        SingleConditionBean.newSingleConditionBean().withCondition(PARAMETER_CAPTURE_CONDITION_URL + "/space" +
                                                "?pageId={page.id}&spaceKey={space.key}&spaceId={space.id}").build()
                                )
                                .build())
                .addRoute(onlyBarneyConditionUrl, new CheckUsernameConditionServlet(barney))
                .addRoute(onlyBettyConditionUrl, new CheckUsernameConditionServlet(betty))
                .addRoute(PARAMETER_CAPTURE_CONDITION_URL, PARAMETER_CAPTURING_SERVLET)
                .addRoute(PARAMETER_CAPTURE_CONDITION_URL + "/space", PARAMETER_CAPTURING_SERVLET2)
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
    public void bettyCanSeeBettyWebItem() throws Exception
    {
        login(betty);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(onlyBettyWebItem), Optional.of("help-menu-link"));
        Assert.assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeBettyWebItem() throws Exception
    {
        login(barney);

        visitEditPage();
        Assert.assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(onlyBettyWebItem)));
    }

    @Test
    public void adminCannotSeeBettyWebItem() throws Exception
    {
        login(testUserFactory.admin());

        visitEditPage();
        Assert.assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(onlyBettyWebItem)));
    }

    @Test
    public void bettyCanSeeBettyAndBarneyWebItem() throws Exception
    {
        login(betty);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(bettyAndBarneyWebItem), Optional.of("help-menu-link"));
        Assert.assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCanSeeBettyAndBarneyWebItem() throws Exception
    {
        login(barney);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(bettyAndBarneyWebItem), Optional.of("help-menu-link"));
        Assert.assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyAndBarneyWebItem() throws Exception
    {
        login(testUserFactory.admin());

        visitEditPage();
        Assert.assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(bettyAndBarneyWebItem)));
    }

    @Test
    public void bettyCanSeeAdminRightsWebItem() throws Exception
    {
        login(betty);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.of("help-menu-link"));
        Assert.assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeAdminRightsWebItem() throws Exception
    {
        login(barney);
        visitEditPage();
        Assert.assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM)));
    }

    @Test
    public void adminCanSeeAdminRightsWebItem() throws Exception
    {
        login(testUserFactory.admin());

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.of("help-menu-link"));
        Assert.assertNotNull("Web item should be found", webItem);
    }

    private ConfluenceEditPage navigateToEditPageAndVerifyParameterCapturingWebItem(TestUser user) throws Exception
    {
        login(user);

        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(CONTEXT_PARAMETERIZED_WEBITEM), Optional.of("help-menu-link"));
        Assert.assertNotNull("Web item should be found", webItem);
        return editPage;
    }

    @Test
    public void standardParametersArePassedToConditions() throws Exception
    {
        TestUser user = testUserFactory.basicUser();
        navigateToEditPageAndVerifyParameterCapturingWebItem(user);

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("lic"), IsEqual.equalTo("none")));
        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("cp"), IsEqual.equalTo("/confluence")));
        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("tz"), ParamMatchers.isTimeZone()));
        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("loc"), ParamMatchers.isLocale()));
        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("user_id"), IsEqual.equalTo(user.getDisplayName())));
    }

    @Test
    public void contextParametersArePassedToConditions() throws Exception
    {
        ConfluenceEditPage editPage = navigateToEditPageAndVerifyParameterCapturingWebItem(testUserFactory.basicUser());

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("pageId"), IsEqual.equalTo(editPage.getPageId())));
        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("spaceKey"), IsEqual.equalTo("ds")));
    }

    @Test
    public void spaceContextParametersArePassedToConditions() throws Exception
    {
        login(testUserFactory.basicUser());
        ConfluenceEditPage editPage = visitEditPage();

        connectPageOperations.findWebPanel(getModuleKey(SPACE_CONTEXT_PARAMETERIZED_WEB_PANEL));
        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET2.getParamsFromLastRequest();

        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("pageId"), IsEqual.equalTo(editPage.getPageId())));
        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("spaceKey"), IsEqual.equalTo("ds")));
        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("spaceId"), CoreMatchers.both(CoreMatchers.not(IsEqual.equalTo(""))).and(CoreMatchers.not(CoreMatchers.nullValue()))));
        Assert.assertThat(conditionParams, IsMapContaining.hasEntry(IsEqual.equalTo("spaceId"), IsLong.isLong()));
    }

    @Test
    public void versionIsIncluded() throws Exception
    {
        navigateToEditPageAndVerifyParameterCapturingWebItem(testUserFactory.basicUser());

        String version = PARAMETER_CAPTURING_SERVLET.getHttpHeaderFromLastRequest(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION).get();

        Assert.assertThat(version, ParamMatchers.isVersionNumber());
    }


    private ConfluenceEditPage visitEditPage() throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(Option.some(testUserFactory.basicUser()), "ds", "Page with webpanel", "some page content");

        return product.visit(ConfluenceEditPage.class, pageData.getId());
    }

    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(),module);
    }
}
