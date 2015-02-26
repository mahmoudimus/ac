package it.modules.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RemoteMacroEditor;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilEquals;
import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;

/**
 * Integration tests for the JavaScript API method confluence.closeMacroEditor().
 */
public class TestConfluenceCloseMacroEditor extends AbstractConfluenceWebDriverTest
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
        CreatePage createPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), TestSpace.DEMO);
        createPage.setTitle(randomName("confluence.closeMacroEditor() Test"));
        MacroBrowserDialog macroBrowserDialog = createPage.getEditor().openMacroBrowser();
        selectMacro(macroBrowserDialog, MACRO_NAME);

        RemoteMacroEditor remoteMacroEditor = findRemoteMacroEditor();
        remoteMacroEditor.closeMacroEditor();
    }

    private void selectMacro(MacroBrowserDialog macroBrowserDialog, String macroname)
    {
        MacroItem item = macroBrowserDialog.searchForFirst(macroname);
        waitUntilEquals(macroname, item.getTitle());
        item.select();
    }

    private RemoteMacroEditor findRemoteMacroEditor()
    {
        String id = editorMacroModuleBean.getRawKey();
        return connectPageOperations.findIFrameComponent(id, RemoteMacroEditor.class);
    }
}
