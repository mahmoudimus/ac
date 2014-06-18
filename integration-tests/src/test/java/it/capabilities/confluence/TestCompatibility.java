package it.capabilities.confluence;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

import static com.atlassian.fugue.Option.some;
import static it.TestConstants.ADMIN_USERNAME;

public class TestCompatibility extends ConfluenceWebDriverTestBase
{
    private static final Option<ConfluenceOps.ConfluenceUser> ADMIN_CONFLUENCE_USER = some(new ConfluenceOps.ConfluenceUser(ADMIN_USERNAME, ADMIN_USERNAME));
    private static final String STORAGE_FORMAT = "<p>\n" +
            "<ac:structured-macro ac:name=\"map\"><ac:parameter ac:name=\"data\">macro data</ac:parameter></ac:structured-macro>\n" +
            "</p>";
    private static final String TEST_SPACE = "ds";
    private static final String MACRO_KEY = "map";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), RemotePluginUtils.randomPluginKey())
                .setAuthenticationToNone()
                .addModules("dynamicContentMacros", DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                                .withKey(MACRO_KEY)
                                .withUrl("/maps?data={data}")
                                .withName(new I18nProperty("Google Maps", ""))
                                .withParameters(MacroParameterBean.newMacroParameterBean()
                                        .withIdentifier("data")
                                        .withName(new I18nProperty("Data", null))
                                        .build())
                                .build()
                )
                .addRoute("/maps", ConnectAppServlets.echoQueryParametersServlet())
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
    public void macroIsRendered() throws Exception
    {
        loginAsAdmin();
        createAndVisitPage(STORAGE_FORMAT);
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(getModuleKey(MACRO_KEY));
        String macroParameter = renderedMacro.getIFrameElementText("data");
        Assert.assertEquals("data: macro data", macroParameter);
    }

    private void createAndVisitPage(String pageContent) throws MalformedURLException, XmlRpcFault
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(ADMIN_CONFLUENCE_USER, TEST_SPACE,
                "macro page", pageContent);
        product.visit(ConfluenceViewPage.class, pageData.getId());
    }

    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), module);
    }
}
