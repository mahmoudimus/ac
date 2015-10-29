package it.confluence.macro;

import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.apache.commons.lang.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

public class TestCompatibility extends ConfluenceWebDriverTestBase
{
    private static final String STORAGE_FORMAT = "<p>\n" +
            "<ac:structured-macro ac:name=\"map\"><ac:parameter ac:name=\"data\">macro data</ac:parameter></ac:structured-macro>\n" +
            "</p>";

    private static final String MACRO_KEY = "map";
    private static final String MACRO_KEY_2 = "something-else";
    private static final String MACRO_NAME_2 = "Something Else";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("dynamicContentMacros",
                        DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                                .withKey(MACRO_KEY)
                                .withUrl("/maps?data={data}")
                                .withName(new I18nProperty("Google Maps", null))
                                .withParameters(MacroParameterBean.newMacroParameterBean()
                                        .withIdentifier("data")
                                        .withName(new I18nProperty("Data", null))
                                        .build())
                                .build(),
                        DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                                .withKey(MACRO_KEY_2)
                                .withAliases(MACRO_KEY)
                                .withUrl("/maps")
                                .withName(new I18nProperty(MACRO_NAME_2, null))
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
        login(testUserFactory.basicUser());
        createAndVisitPage(STORAGE_FORMAT);
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(MACRO_KEY);
        String macroParameter = renderedMacro.getIFrameElementText("data");
        Assert.assertEquals("data: macro data", macroParameter);
    }

    @Test
    public void testAliasIsNotPersisted() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), ConfluenceWebDriverTestBase.TestSpace.DEMO);
        editorPage.setTitle(RandomStringUtils.randomAlphanumeric(8));
        selectMacroAndSave(editorPage, MACRO_NAME_2);
        ViewPage page = editorPage.save();

        String content = rpc.getPageContent(page.getPageId());
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select("ac|structured-macro");
        Assert.assertEquals("only one macro found", 1, elements.size());
        Assert.assertEquals("name set correct (not alias)", MACRO_KEY_2, elements.get(0).attr("ac:name"));
    }

    private void createAndVisitPage(String pageContent) throws MalformedURLException, XmlRpcFault
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(Option.some(testUserFactory.basicUser()),
                TestSpace.DEMO.getKey(), "macro page", pageContent);
        product.visit(ConfluenceViewPage.class, pageData.getId());
    }
}
