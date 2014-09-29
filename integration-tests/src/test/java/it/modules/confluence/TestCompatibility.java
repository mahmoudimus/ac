package it.modules.confluence;

import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.util.TestUser;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static it.servlet.ConnectAppServlets.echoQueryParametersServlet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;

public class TestCompatibility extends AbstractConfluenceWebDriverTest
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
                        newDynamicContentMacroModuleBean()
                                .withKey(MACRO_KEY)
                                .withUrl("/maps?data={data}")
                                .withName(new I18nProperty("Google Maps", ""))
                                .withParameters(MacroParameterBean.newMacroParameterBean()
                                        .withIdentifier("data")
                                        .withName(new I18nProperty("Data", null))
                                        .build())
                                .build(),
                        newDynamicContentMacroModuleBean()
                                .withKey(MACRO_KEY_2)
                                .withAliases(MACRO_KEY)
                                .withUrl("/maps")
                                .withName(new I18nProperty(MACRO_NAME_2, ""))
                                .build()
                )
                .addRoute("/maps", echoQueryParametersServlet())
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
        login(it.util.TestUser.ADMIN);
        createAndVisitPage(STORAGE_FORMAT);
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(MACRO_KEY);
        String macroParameter = renderedMacro.getIFrameElementText("data");
        assertEquals("data: macro data", macroParameter);
    }

    @Test
    public void testAliasIsNotPersisted() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), AbstractConfluenceWebDriverTest.TestSpace.DEMO);
        editorPage.setTitle(RandomStringUtils.randomAlphanumeric(8));

        selectMacroAndSave(editorPage, MACRO_NAME_2);

        ViewPage page = editorPage.save();
        String content = rpc.getPageContent(page.getPageId());
        assertThat(content, endsWith("<p><ac:structured-macro ac:name=\"something-else\" /></p>"));
    }

    private void createAndVisitPage(String pageContent) throws MalformedURLException, XmlRpcFault
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(TestUser.ADMIN),
                TestSpace.DEMO.getKey(), "macro page", pageContent);
        product.visit(ConfluenceViewPage.class, pageData.getId());
    }

}
