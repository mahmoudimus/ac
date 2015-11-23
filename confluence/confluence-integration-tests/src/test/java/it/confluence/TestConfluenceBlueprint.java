package it.confluence;

import java.io.IOException;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redstone.xmlrpc.XmlRpcFault;

import static com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean.newBlueprintModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean.newBlueprintTemplateBeanBuilder;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintTemplateServlet;
import static org.junit.Assert.assertTrue;

/**
 * Integration test that loads a blueprint addon and uses it in confluence.
 */
public final class TestConfluenceBlueprint extends ConfluenceWebDriverTestBase
{
    private static ConnectRunner runner;
    private static String completeKey;

    @BeforeClass
    public static void setupConfluenceAndStartConnectAddOn() throws Exception
    {
        String key = AddonTestUtils.randomAddOnKey();
        String moduleKey = "my-blueprint";
        completeKey = "com.atlassian.plugins.atlassian-connect-plugin:" + ModuleKeyUtils.addonAndModuleKey(key, moduleKey) + "-web-item";
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(),
                key)
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule("blueprints",
                        newBlueprintModuleBean()
                                .withName(new I18nProperty("My Blueprint", null))
                                .withKey(moduleKey)
                                .withTemplate(newBlueprintTemplateBeanBuilder()
                                        .withUrl("/template.xml")
                                        .build())
                                .build())
                .addRoute("/template.xml", blueprintTemplateServlet())
                .addScope(ScopeName.READ)
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testRemoteSimpleBlueprintVisibleInDialog() throws XmlRpcFault, IOException
    {
        login(testUserFactory.basicUser());
        product.visit(DashboardPage.class).createDialog.click();
        product.getPageBinder().bind(CreateContentDialog.class).waitForBlueprint(completeKey);
    }

    @Test
    public void testRemoteSimpleBlueprintCanCreatePage() throws XmlRpcFault, IOException
    {
        login(testUserFactory.basicUser());
        product.visit(DashboardPage.class).createDialog.click();
        assertTrue("new page includes blueprint content",
                product.getPageBinder()
                        .bind(CreateContentDialog.class)
                        .createWithBlueprint(completeKey)
                        .getEditor()
                        .getContent()
                        .getTimedHtml()
                        .byDefaultTimeout().contains("Hello Blueprint"));
    }


}
