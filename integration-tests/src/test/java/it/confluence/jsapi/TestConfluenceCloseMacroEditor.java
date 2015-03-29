package it.confluence.jsapi;

import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RemoteMacroEditor;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.AbstractConfluenceWebDriverTest;
import it.confluence.macro.AbstractContentMacroTest;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;

/**
 * Integration tests for the JavaScript API method confluence.closeMacroEditor().
 */
public class TestConfluenceCloseMacroEditor extends AbstractConfluenceWebDriverTest
{
    private static final String MACRO_NAME = AbstractContentMacroTest.EDITOR_MACRO_NAME;

    private static ConnectRunner addon;
    private static StaticContentMacroModuleBean editorMacroModuleBean;

    private CreatePage createPage;

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

    // clean up so that we don't get "org.openqa.selenium.UnhandledAlertException: unexpected alert open" in tests
    @Before
    @After
    public void cleanUpAroundEachTest()
    {
        AbstractContentMacroTest.resetEditorState(createPage, null);
        createPage = null;
    }

    @Test
    public void shouldCloseMacroEditorWhenInsertingMacroOnNewPage()
    {
        createPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        selectMacro(createPage, MACRO_NAME, new Runnable()
        {

            @Override
            public void run()
            {
                RemoteMacroEditor remoteMacroEditor = findRemoteMacroEditor();
                remoteMacroEditor.closeMacroEditor();
            }
        });
    }

    private RemoteMacroEditor findRemoteMacroEditor()
    {
        String id = editorMacroModuleBean.getRawKey();
        return connectPageOperations.findIFrameComponent(id, RemoteMacroEditor.class);
    }
}
