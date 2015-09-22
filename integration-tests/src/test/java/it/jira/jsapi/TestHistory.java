package it.jira.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteHistoryGeneralPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestHistory extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String PAGE_NAME = "History general page";
    private static final String GENERAL_PAGE_KEY = "my-history-general-page";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(GENERAL_PAGE_KEY)
                                .withUrl("/history-general-page")
                                .withConditions(toggleableConditionBean())
                                .withWeight(1234)
                                .build())
                .addRoute("/history-general-page", ConnectAppServlets.historyServlet())
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
    public void testHistoryPushState() throws MalformedURLException, URISyntaxException
    {
        RemoteHistoryGeneralPage addonHistoryGeneralPage = loginAndVisit(testUserFactory.basicUser(), RemoteHistoryGeneralPage.class, PLUGIN_KEY, GENERAL_PAGE_KEY);

        URI url = new URI(addonHistoryGeneralPage.hostUrl());
        assertThat(url.getPath(), is("/jira" + IframeUtils.iframeServletPath(PLUGIN_KEY, GENERAL_PAGE_KEY)));
        assertThat(url.getFragment(), isEmptyOrNullString());

        addonHistoryGeneralPage.javascriptPushState();
        URI newUrl = new URI(addonHistoryGeneralPage.hostUrl());
        assertThat(newUrl.getFragment(), is("!mypushedstate0"));
    }

    @Test
    public void testHistoryPopState() throws MalformedURLException, URISyntaxException
    {
        RemoteHistoryGeneralPage addonHistoryGeneralPage = loginAndVisit(testUserFactory.basicUser(), RemoteHistoryGeneralPage.class, PLUGIN_KEY, GENERAL_PAGE_KEY);

        addonHistoryGeneralPage.javascriptPushState();
        URI newUrl = new URI(addonHistoryGeneralPage.hostUrl());
        assertThat(newUrl.getFragment(), is("!mypushedstate0"));

        addonHistoryGeneralPage.browserBack();
        assertThat(addonHistoryGeneralPage.logOldUrl(), is("mypushedstate0"));
        assertThat(addonHistoryGeneralPage.logNewUrl(), isEmptyString());
    }

    @Test
    public void testHistoryPopStateDoesNotRunOnPushState() throws MalformedURLException, URISyntaxException
    {
        RemoteHistoryGeneralPage addonHistoryGeneralPage = loginAndVisit(testUserFactory.basicUser(), RemoteHistoryGeneralPage.class, PLUGIN_KEY, GENERAL_PAGE_KEY);

        addonHistoryGeneralPage.javascriptPushState();
        addonHistoryGeneralPage.clearLog();
        addonHistoryGeneralPage.javascriptPushState();
        assertEquals(addonHistoryGeneralPage.logMessage(), "history pushstate1");
    }

    @Test
    public void testHistoryPushForward() throws MalformedURLException, URISyntaxException
    {
        RemoteHistoryGeneralPage addonHistoryGeneralPage = loginAndVisit(testUserFactory.basicUser(), RemoteHistoryGeneralPage.class, PLUGIN_KEY, GENERAL_PAGE_KEY);

        addonHistoryGeneralPage.javascriptPushState();
        addonHistoryGeneralPage.javascriptPushState();
        URI url = new URI(addonHistoryGeneralPage.hostUrl());
        assertThat(url.getFragment(), is("!mypushedstate1"));


        addonHistoryGeneralPage.javascriptBack();
        URI urlBack = new URI(addonHistoryGeneralPage.hostUrl());
        assertThat(urlBack.getFragment(), is("!mypushedstate0"));

        addonHistoryGeneralPage.javascriptForward();
        URI newUrl = new URI(addonHistoryGeneralPage.hostUrl());
        assertThat(newUrl.getFragment(), is("!mypushedstate1"));
    }


}
