package it.capabilities.jira;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;

import com.google.common.base.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


/**
 * @since version
 */
public class TestWebItem extends JiraWebDriverTestBase
{
    private static final String ADDON_WEBITEM = "ac-general-web-item";
    private static final String PRODUCT_WEBITEM = "quick-project-link";
    private static final String ABSOLUTE_WEBITEM = "google-link";

    private static ConnectCapabilitiesRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(),"my-plugin")
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("AC General Web Item", "ac.gen"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("/irwi")
                        .build())
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("Quick project link","ac.qp"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink(product.getProductInstance().getBaseUrl() + "/browse/ACDEV-1234")
                        .build())
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("google link","ac.gl"))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("http://www.google.com")
                        .build()).start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Test
    public void testAbsoluteWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ABSOLUTE_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        webItem.click();

        assertTrue("Web item link should be absolute", webItem.isAbsolute());
        assertEquals("http://www.google.com", webItem.getPath());
    }
}
