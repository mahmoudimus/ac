package it.jira.iframe;

import com.atlassian.jira.projects.pageobjects.webdriver.page.sidebar.SidebarBrowseProjectSubPage;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;


public class TestPaddingProjectTabPanel extends JiraWebDriverTestBase
{
    private static final String ADD_ON_KEY = AddonTestUtils.randomAddOnKey();

    private static ConnectRunner remotePlugin;

    private static ProjectTabPair withoutPadding;
    private static ProjectTabPair withPadding;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        withoutPadding = new ProjectTabPair("connect-provided-web-panel", "connect-provided-link-to-panel", true);
        withPadding = new ProjectTabPair("connect-provided-web-panel2", "connect-provided-link-to-panel2", false);

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), ADD_ON_KEY)
                .setAuthenticationToNone()
                .addJWT(ConnectAppServlets.installHandlerServlet())
                .addModules("webPanels",
                        withoutPadding.getWebPanel(),
                        withPadding.getWebPanel())
                .addModules("webItems",
                        withoutPadding.getWebItem(),
                        withPadding.getWebItem())
                .addRoute("/my-project-tab", ConnectAppServlets.customMessageServlet("Web panel displayed"))
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
    public void noPaddingWhenWithoutPaddingFlagIsSet() throws Exception
    {
        loginAndRun(testUserFactory.basicUser(), new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                product.visit(PluginProvidedProjectSubPage.class, project.getKey(), withoutPadding.getLinkKey());

                int diff = getDistanceBetweenIFrameAndSideBar(withoutPadding.getWebPanelKey());
                assertEquals(0, diff);

                return null;
            }
        });
    }

    @Test
    public void paddingIsPresentWhenNoPaddingFlagIsNotSet() throws Exception
    {
        loginAndRun(testUserFactory.basicUser(), new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                product.visit(PluginProvidedProjectSubPage.class, project.getKey(), withPadding.getLinkKey());

                int diff = getDistanceBetweenIFrameAndSideBar(withPadding.getWebPanelKey());
                assertEquals(20, diff);

                return null;
            }
        });
    }

    private int getDistanceBetweenIFrameAndSideBar(String webPanelKey)
    {
        String id = "easyXDM_embedded-" + ADD_ON_KEY + "__" + webPanelKey + "_provider";
        WebElement element = connectPageOperations.findElement(By.id(id));
        Point iFrameLocation = element.getLocation();

        WebElement sidebar = connectPageOperations.findElementByClass("aui-sidebar-wrapper");

        int sideBarRight = sidebar.getLocation().getX() + sidebar.getSize().getWidth();
        return iFrameLocation.getX() - sideBarRight;
    }

    private static class ProjectTabPair
    {

        private final WebPanelModuleBean webPanel;
        private final WebItemModuleBean webItem;
        private final String linkKey;
        private final String webPanelKey;

        public ProjectTabPair(String webPanelKey, String linkKey, boolean withoutPadding)
        {
            this.webPanelKey = webPanelKey;
            this.linkKey = linkKey;

            webPanel = new WebPanelModuleBeanBuilder()
                    .withKey(webPanelKey)
                    .withLocation(ADD_ON_KEY + "__" + linkKey)
                    .withName(new I18nProperty("my-web-panel", "My Web Panel"))
                    .withUrl("/my-project-tab")
                    .withoutPadding(withoutPadding)
                    .build();

            webItem = new WebItemModuleBeanBuilder()
                    .withContext(AddOnUrlContext.product)
                    .withKey(linkKey)
                    .withLocation("jira.project.sidebar.plugins.navigation")
                    .withName(new I18nProperty("asd", "My Web Item"))
                    .withUrl("/projects/{project.key}?selectedItem=" + ADD_ON_KEY + "__" + linkKey)
                    .build();
        }

        public WebPanelModuleBean getWebPanel()
        {
            return webPanel;
        }

        public WebItemModuleBean getWebItem()
        {
            return webItem;
        }

        public String getLinkKey()
        {
            return linkKey;
        }

        public String getWebPanelKey()
        {
            return webPanelKey;
        }
    }

    public static class PluginProvidedProjectSubPage extends SidebarBrowseProjectSubPage
    {
        private final String linkKey;

        public PluginProvidedProjectSubPage(final String projectKey, final String linkKey)
        {
            super(projectKey);
            this.linkKey = linkKey;
        }

        @Override
        public String getPageId()
        {
            return ADD_ON_KEY + "__" + linkKey;
        }
    }
}
