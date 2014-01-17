package it.capabilities.jira;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.google.common.base.Charsets.UTF_8;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in JIRA
 */
public class TestGeneralPage extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    public static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addModule(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("My Awesome Page", null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg?project_id={project.id}&project_key={project.key}")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
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
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        loginAsAdmin();

        product.visit(JiraViewProjectPage.class, project.getKey());

        JiraGeneralPage viewProjectPage = product.getPageBinder().bind(JiraGeneralPage.class, KEY_MY_AWESOME_PAGE, "My Awesome Page");

        assertThat(viewProjectPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(viewProjectPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/jira/plugins/servlet/ac/my-plugin/pg"));

        assertThat(URLEncodedUtils.parse(url, UTF_8.name()),
                containsInAnyOrder(
                        (NameValuePair) new BasicNameValuePair("project_key", project.getKey()),
                        new BasicNameValuePair("project_id", project.getId())
                )
        );

        RemotePluginTestPage addonContentsPage = viewProjectPage.clickRemotePluginLink();
        assertThat(addonContentsPage.isFullSize(), is(true));
    }


}
