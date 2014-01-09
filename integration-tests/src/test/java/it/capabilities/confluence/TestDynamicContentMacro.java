package it.capabilities.confluence;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.AbstractConfluenceWebDriverTest;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;

public class TestDynamicContentMacro extends AbstractConfluenceWebDriverTest
{
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapabilities("dynamicContentMacros",
                        newDynamicContentMacroModuleBean()
                                .withUrl("/render-macro")
                                .withName(new I18nProperty("Simple Macro", ""))
                                .build())

                .addRoute("/render-macro", ConnectAppServlets.helloWorldServlet())
                .start();
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
    public void testEditor() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(User.ADMIN, DEMO_SPACE);
        editorPage.setTitle("Title3");
        EditorContent editorContent = editorPage.getContent();
        editorContent.setContent("something");

        final String textContent = editorPage.save().getRenderedContent().getText();
        System.out.println(textContent);
    }
}
