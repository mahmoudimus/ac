package it.confluence;

import java.io.IOException;

import javax.servlet.http.HttpServlet;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import it.servlet.iframe.MustacheServlet;
import redstone.xmlrpc.XmlRpcFault;

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
                        BlueprintModuleBean.newBlueprintModuleBean()
                                .withName(new I18nProperty("My Blueprint", null))
                                .withKey(moduleKey)
                                .withTemplate(BlueprintTemplateBean.newBlueprintTemplateBeanBuilder()
                                        .withUrl("/template.xml")
                                        .build())
                                .build())
                .addRoute("/template.xml", blueprintTemplateServlet())
                .addScope(ScopeName.READ)
                .start();
    }

    public static HttpServlet blueprintTemplateServlet()
    {
        return ConnectAppServlets.wrapContextAwareServlet(new MustacheServlet("confluence/test-blueprint.xml"));
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
        Assert.assertTrue("new page includes blueprint content",
                product.getPageBinder()
                        .bind(CreateContentDialog.class)
                        .createWithBlueprint(completeKey)
                        .getEditor()
                        .getContent()
                        .getTimedHtml()
                        .byDefaultTimeout().contains("Hello Blueprint"));
    }


}
