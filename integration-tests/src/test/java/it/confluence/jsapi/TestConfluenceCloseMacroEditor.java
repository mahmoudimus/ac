package it.confluence.jsapi;

import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RemoteMacroEditorDialog;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.util.ConnectTestUserFactory;
import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.macro.AbstractContentMacroTest;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;

/**
 * Integration tests for the JavaScript API method confluence.closeMacroEditor().
 */
public class TestConfluenceCloseMacroEditor extends ConfluenceWebDriverTestBase
{
    private static final String MACRO_NAME = AbstractContentMacroTest.EDITOR_MACRO_NAME;

    private static ConnectRunner addon;
    private static StaticContentMacroModuleBean editorMacroModuleBean;

    @BeforeClass
    public static void startAddon() throws Exception
    {
        editorMacroModuleBean = AbstractContentMacroTest.createEditorMacro(newStaticContentMacroModuleBean());

        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addModules("staticContentMacros", editorMacroModuleBean)
                .addRoute("/render-editor", ConnectAppServlets.macroEditor())
                .addRoute("/echo/params", ConnectAppServlets.echoQueryParametersServlet())
                .start();
    }

    @AfterClass
    public static void stopAddon() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void shouldCloseMacroEditorWhenInsertingMacroOnNewPage()
    {
        CreatePage createPage = getProduct().loginAndCreatePage(testUserFactory.basicUser().confUser(), TestSpace.DEMO);
        selectMacro(createPage, MACRO_NAME, new Runnable()
        {

            @Override
            public void run()
            {
                RemoteMacroEditorDialog remoteMacroEditor = connectPageOperations.findDialog(
                        AbstractContentMacroTest.EDITOR_MACRO_KEY, RemoteMacroEditorDialog.class);
                remoteMacroEditor.closeMacroEditorAndWaitUntilHidden();
            }
        });
        createPage.cancel();
    }
}
