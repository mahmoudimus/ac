package it.confluence.jsapi;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.it.Page;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteDialog;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.util.concurrent.Promise;
import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.servlet.ConfluenceAppServlets;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;
import static com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor.toConfluenceUser;
import static junit.framework.TestCase.assertEquals;

public class TestNavigatorContext extends ConfluenceWebDriverTestBase
{
    /*
     These tests work by navigating to a page and then attempting to bind a page object to that page.
     If the navigation itself fails, the page will not bind successfully and the test will fail.
     */

    private static List<Exception> setupFailure = new ArrayList<>();
    private static final String WEB_ITEM_KEY = "ac-navigator-web-item";
    private static ConnectRunner remotePlugin;

    private static Promise<Content> createdPage;
    private static Space space;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        String spaceKey = "NAVTEST" + System.currentTimeMillis();
        space = restClient.spaces().create(Space.builder().key(spaceKey).name("Nav Space").build(), false).get();
        createdPage = restClient.content().create(Content.builder(ContentType.PAGE)
                .space(space)
                .body("<p>Page content</p>", ContentRepresentation.STORAGE)
                .title("Page")
                .build());

        try
        {
            remotePlugin = new ConnectRunner(ConfluenceWebDriverTestBase.product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                    .setAuthenticationToNone()
                    .addModule("webItems",
                            newWebItemBean()
                                    .withName(new I18nProperty("Context", null))
                                    .withUrl("/nvg-context")
                                    .withKey(WEB_ITEM_KEY)
                                    .withLocation("system.header/left")
                                    .withTarget(newWebItemTargetBean()
                                            .withType(WebItemTargetType.dialog)
                                            .build())
                                    .build()
                    )
                    .addRoute("/nvg-context", ConfluenceAppServlets.navigatorContextServlet())
                    .start();
        }
        catch (Exception ex)
        {
            setupFailure.add(ex);
        }

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
    public void setUp() throws Exception
    {
        if (!setupFailure.isEmpty())
        {
            throw setupFailure.get(0);
        }
    }

    @Test
    public void testGetCurrentContextOfCreatePage() throws Exception
    {
        getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), TestSpace.DEMO);
        RemoteDialog dialog = openDialog();
        assertEquals("unknown", dialog.getIFrameElement("ac-target"));
    }

    @Test
    public void testGetCurrentContextOfEditPage() throws Exception
    {
        Content page = createPage(randomName("testGetCurrentContextOfEditPage"), "");
        getProduct().loginAndEdit(toConfluenceUser(testUserFactory.basicUser()), new Page(page.getId().asLong()));
        RemoteDialog dialog = openDialog();
        assertEquals("contentedit", dialog.getIFrameElement("ac-target"));
        assertEquals(String.valueOf(page.getId().asLong()), dialog.getIFrameElement("ac-contentId"));
        assertEquals("page", dialog.getIFrameElement("ac-contentType"));
    }

    @Test
    public void testGetCurrentContextOfViewPage() throws Exception
    {
        Content page = createPage(randomName("testGetCurrentContextOfViewPage"), "");
        getProduct().loginAndView(toConfluenceUser(testUserFactory.basicUser()), new Page(page.getId().asLong()));
        RemoteDialog dialog = openDialog();
        assertEquals("contentview", dialog.getIFrameElement("ac-target"));
        assertEquals(String.valueOf(page.getId().asLong()), dialog.getIFrameElement("ac-contentId"));
        assertEquals("page", dialog.getIFrameElement("ac-contentType"));
    }

    private RemoteDialog openDialog() {
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(WEB_ITEM_KEY), Optional.<String>empty());
        webItem.click();
        return product.getPageBinder().bind(RemoteDialog.class);
    }

    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), module);
    }

}
