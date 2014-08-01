package it.modules.confluence.condition;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.modules.confluence.AbstractConfluenceWebDriverTest;
import it.servlet.condition.CheckUsernameConditionServlet;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.matcher.IsLong.isLong;
import static it.matcher.ParamMatchers.isLocale;
import static it.matcher.ParamMatchers.isTimeZone;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class TestConfluenceConditions extends AbstractConfluenceWebDriverTest
{
    private static ConnectRunner remotePlugin;

    private static final String ONLY_BETTY_WEBITEM = "only-betty";
    private static final String BETTY_AND_BARNEY_WEBITEM = "betty-and-barney";
    private static final String ADMIN_RIGHTS_WEBITEM = "admin-rights";
    private static final String CONTEXT_PARAMETERIZED_WEBITEM = "context-parameterized";
    public static final String SPACE_CONTEXT_PARAMETERIZED_WEB_PANEL = CONTEXT_PARAMETERIZED_WEBITEM + "-space";

    private static final String ONLY_BETTY_CONDITION_URL = "/onlyBettyCondition";
    private static final String ONLY_BARNEY_CONDITION_URL = "/onlyBarneyCondition";
    private static final String PARAMETER_CAPTURE_CONDITION_URL = "/parameterCapture";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();
    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET2 = new ParameterCapturingConditionServlet(); // a 2nd to receive condition requests on the same page

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("Only Betty", ONLY_BETTY_WEBITEM))
                                .withKey(ONLY_BETTY_WEBITEM)
                                .withLocation("system.browse")
                                .withWeight(1)
                                .withUrl("http://www.google.com")
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                        newSingleConditionBean().withCondition(ONLY_BETTY_CONDITION_URL).build()
                                )
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Betty And Barney", BETTY_AND_BARNEY_WEBITEM))
                                .withKey(BETTY_AND_BARNEY_WEBITEM)
                                .withLocation("system.browse")
                                .withWeight(1)
                                .withUrl("http://www.google.com")
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                        newCompositeConditionBean()
                                                .withType(CompositeConditionType.OR)
                                                .withConditions(
                                                        newSingleConditionBean().withCondition(ONLY_BETTY_CONDITION_URL).build(),
                                                        newSingleConditionBean().withCondition(ONLY_BARNEY_CONDITION_URL).build()
                                                ).build()
                                ).build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Admin Rights", ADMIN_RIGHTS_WEBITEM))
                                .withKey(ADMIN_RIGHTS_WEBITEM)
                                .withLocation("system.browse")
                                .withWeight(1)
                                .withUrl("http://www.google.com")
                                .withConditions(
                                        newSingleConditionBean().withCondition("user_is_confluence_administrator").build()
                                )
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Context Parameterized", CONTEXT_PARAMETERIZED_WEBITEM))
                                .withKey(CONTEXT_PARAMETERIZED_WEBITEM)
                                .withLocation("system.browse")
                                .withContext(AddOnUrlContext.addon)
                                .withWeight(1)
                                .withUrl("/somewhere")
                                .withConditions(
                                        newSingleConditionBean().withCondition(PARAMETER_CAPTURE_CONDITION_URL +
                                                "?pageId={page.id}&spaceKey={space.key}").build()
                                )
                                .build())
                .addModules("webPanels",
                        newWebPanelBean()
                                .withName(new I18nProperty("Space Context Parameterized", SPACE_CONTEXT_PARAMETERIZED_WEB_PANEL))
                                .withKey(SPACE_CONTEXT_PARAMETERIZED_WEB_PANEL)
                                .withLocation("atl.general") // this location needs testing for space params; see AC-1018
                                .withUrl("/somewhere-else")
                                .withConditions(
                                        newSingleConditionBean().withCondition(PARAMETER_CAPTURE_CONDITION_URL + "/space" +
                                                "?pageId={page.id}&spaceKey={space.key}&spaceId={space.id}").build()
                                )
                                .build())
                .addRoute(ONLY_BARNEY_CONDITION_URL, new CheckUsernameConditionServlet(TestUser.BARNEY))
                .addRoute(ONLY_BETTY_CONDITION_URL, new CheckUsernameConditionServlet(TestUser.BETTY))
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
        login(TestUser.BETTY);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(ONLY_BETTY_WEBITEM), Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeBettyWebItem() throws Exception
    {
        login(TestUser.BARNEY);

        visitEditPage();
        assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(ONLY_BETTY_WEBITEM)));
    }

    @Test
    public void adminCannotSeeBettyWebItem() throws Exception
    {
        login(TestUser.ADMIN);

        visitEditPage();
        assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(ONLY_BETTY_WEBITEM)));
    }

    @Test
    public void bettyCanSeeBettyAndBarneyWebItem() throws Exception
    {
        login(TestUser.BETTY);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(BETTY_AND_BARNEY_WEBITEM), Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCanSeeBettyAndBarneyWebItem() throws Exception
    {
        login(TestUser.BARNEY);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(BETTY_AND_BARNEY_WEBITEM), Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyAndBarneyWebItem() throws Exception
    {
        login(TestUser.ADMIN);

        visitEditPage();
        assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(BETTY_AND_BARNEY_WEBITEM)));
    }

    @Test
    public void bettyCanSeeAdminRightsWebItem() throws Exception
    {
        login(TestUser.BETTY);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeAdminRightsWebItem() throws Exception
    {
        login(TestUser.BARNEY);
        visitEditPage();
        assertFalse("Web item should NOT be found", connectPageOperations.existsWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM)));
    }

    @Test
    public void adminCanSeeAdminRightsWebItem() throws Exception
    {
        login(TestUser.ADMIN);

        visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    private ConfluenceEditPage navigateToEditPageAndVerifyParameterCapturingWebItem() throws Exception
    {
        login(TestUser.ADMIN);

        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(CONTEXT_PARAMETERIZED_WEBITEM), Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
        return editPage;
    }

    @Test
    public void standardParametersArePassedToConditions() throws Exception
    {
        navigateToEditPageAndVerifyParameterCapturingWebItem();

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("lic"), equalTo("none")));
        assertThat(conditionParams, hasEntry(equalTo("cp"), equalTo("/confluence")));
        assertThat(conditionParams, hasEntry(equalTo("tz"), isTimeZone()));
        assertThat(conditionParams, hasEntry(equalTo("loc"), isLocale()));
        assertThat(conditionParams, hasEntry(equalTo("user_id"), equalTo("admin")));
    }

    @Test
    public void contextParametersArePassedToConditions() throws Exception
    {
        ConfluenceEditPage editPage = navigateToEditPageAndVerifyParameterCapturingWebItem();

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("pageId"), equalTo(editPage.getPageId())));
        assertThat(conditionParams, hasEntry(equalTo("spaceKey"), equalTo("ds")));
    }

    @Test
    public void spaceContextParametersArePassedToConditions() throws Exception
    {
        login(TestUser.ADMIN);
        ConfluenceEditPage editPage = visitEditPage();
        // NOTE: we don't actually need the web panel to test its condition invocation

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET2.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("pageId"), equalTo(editPage.getPageId())));
        assertThat(conditionParams, hasEntry(equalTo("spaceKey"), equalTo("ds")));
        assertThat(conditionParams, hasEntry(equalTo("spaceId"), both(not(equalTo(""))).and(not(nullValue()))));
        assertThat(conditionParams, hasEntry(equalTo("spaceId"), isLong()));
    }

    private ConfluenceEditPage visitEditPage() throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(TestUser.ADMIN), "ds", "Page with webpanel", "some page content");

        return product.visit(ConfluenceEditPage.class, pageData.getId());
    }

    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(),module);
    }
}
