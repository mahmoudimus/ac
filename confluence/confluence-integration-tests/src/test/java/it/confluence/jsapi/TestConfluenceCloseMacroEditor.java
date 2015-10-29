package it.confluence.jsapi;

import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.connect.test.confluence.pageobjects.RemoteMacroEditorDialog;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.macro.AbstractContentMacroTest;
import it.confluence.servlet.ConfluenceAppServlets;
import it.servlet.ConnectAppServlets;

/**
 * Integration tests for the JavaScript API method confluence.closeMacroEditor().
 */
public class TestConfluenceCloseMacroEditor extends ConfluenceWebDriverTestBase
{

    private static ConnectRunner addon;
    private static StaticContentMacroModuleBean editorMacroModuleBean;

    @BeforeClass
    public static void startAddon() throws Exception
    {
        editorMacroModuleBean = AbstractContentMacroTest.createEditorMacro(StaticContentMacroModuleBean.newStaticContentMacroModuleBean());

        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addModules("staticContentMacros", editorMacroModuleBean)
                .addRoute("/render-editor", ConfluenceAppServlets.macroEditor())
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
        selectMacro(createPage, editorMacroModuleBean.getName().getRawValue(), new Runnable()
        {

            @Override
            public void run()
            {
                RemoteMacroEditorDialog remoteMacroEditor = connectPageOperations.findDialog(
                        editorMacroModuleBean.getRawKey(), RemoteMacroEditorDialog.class);
                remoteMacroEditor.closeMacroEditorAndWaitUntilHidden();
            }
        });
        createPage.cancel();
    }
}
