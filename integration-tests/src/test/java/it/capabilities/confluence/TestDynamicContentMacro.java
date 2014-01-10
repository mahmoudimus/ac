package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroBrowserDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroForm;
import com.atlassian.plugin.connect.test.pageobjects.confluence.MacroList;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class TestDynamicContentMacro extends AbstractConfluenceWebDriverTest
{
    private static final String SIMPLE_MACRO_NAME = "Simple Macro";
    private static final String SIMPLE_MACRO_ALIAS = "alias";

    private static final String PARAMETER_MACRO_NAME = "Parameter Macro";

    private static ConnectRunner remotePlugin;

    private static DynamicContentMacroModuleBean simpleMacro;
    private static DynamicContentMacroModuleBean parameterMacro;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        simpleMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro")
                .withName(new I18nProperty(SIMPLE_MACRO_NAME, ""))
                .withIcon(newIconBean()
                        .withUrl("images/macro-icon.png")
                        .build()
                )
                .withAliases(SIMPLE_MACRO_ALIAS)
                .build();

        parameterMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro")
                .withName(new I18nProperty(PARAMETER_MACRO_NAME, ""))
                .withParameters(
                        newMacroParameterBean()
                                .withIdentifier("attachment")
                                .withName(new I18nProperty("Attachment", ""))
                                .withType("attachment")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("boolean")
                                .withName(new I18nProperty("Boolean", ""))
                                .withType("boolean")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("content")
                                .withName(new I18nProperty("Confluence Content", ""))
                                .withType("confluence-content")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("enum")
                                .withName(new I18nProperty("Enum", ""))
                                .withType("enum")
                                .withValues("val1", "val2")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("spacekey")
                                .withName(new I18nProperty("Space Key", ""))
                                .withType("spacekey")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("string")
                                .withName(new I18nProperty("String", ""))
                                .withType("string")
                                .build(),
                        newMacroParameterBean()
                                .withIdentifier("username")
                                .withName(new I18nProperty("User Name", ""))
                                .withType("username")
                                .build()
                )
                .build();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapabilities("dynamicContentMacros", simpleMacro, parameterMacro)
                .addRoute("/render-macro", ConnectAppServlets.helloWorldServlet())
                .start();

        overridePageObjects();
    }

    private static void overridePageObjects()
    {
        product.getPageBinder().override(MacroBrowserDialog.class, ConfluenceMacroBrowserDialog.class);
        product.getPageBinder().override(EditorContent.class, ConfluenceEditorContent.class);
        product.getPageBinder().override(MacroForm.class, ConfluenceMacroForm.class);
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testMacroIsListed() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(SIMPLE_MACRO_NAME);

        assertThat(macro, is(not(nullValue())));
    }

    @Test
    public void testMacroIsRendered() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Simple Macro on Page6");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(SIMPLE_MACRO_NAME);
        macro.select();
        macroBrowser.clickInsert();

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(simpleMacro.getKey(), 0);
        String content = renderedMacro.getIFrameElementText("hello-world-message");
        rpc.removePage(savedPage.getPageId());

        assertThat(content, is("Hello world"));
    }

    @Test
    public void testAlias() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();

        MacroList macroList = editorContent.autocompleteMacro(SIMPLE_MACRO_ALIAS);
        assertThat(macroList.hasEntryWithKey(simpleMacro.getKey()), is(true));
    }

    @Test
    public void testParameterTypes() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(PARAMETER_MACRO_NAME);
        ConfluenceMacroForm macroForm = (ConfluenceMacroForm) macro.select();

        List<String> parameterNames = macroForm.getParameterNames();
        assertThat(parameterNames, containsInAnyOrder(
                "attachment",
                "boolean",
                "content",
                "enum",
                "spacekey",
                "string",
                "username"
        ));
    }


}
