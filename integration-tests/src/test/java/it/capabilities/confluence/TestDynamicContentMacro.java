package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroBrowserDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroForm;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestDynamicContentMacro extends AbstractContentMacroTest
{
    private static final String LONG_BODY_MACRO_NAME = "Long Body Macro";
    private static final String LONG_BODY_MACRO_KEY = "long-body-macro";

    private static final String SHORT_BODY_MACRO_NAME = "Short Body Macro";
    private static final String SHORT_BODY_MACRO_KEY = "short-body-macro";

    private static final String PARAMETER_MACRO_NAME = "Single Param Macro";
    private static final String PARAMETER_MACRO_KEY = "single-param-macro";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        DynamicContentMacroModuleBean simpleMacro = createSimpleMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean allParameterTypesMacro = createAllParametersMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean featuredMacro = createFeaturedMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean imagePlaceholderMacro = createImagePlaceholderMacro(newDynamicContentMacroModuleBean());

        DynamicContentMacroModuleBean longBodyMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro?hash={macro.hash}")
                .withName(new I18nProperty(LONG_BODY_MACRO_NAME, ""))
                .withBodyType(MacroBodyType.PLAIN_TEXT)
                .build();

        DynamicContentMacroModuleBean shortBodyMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro?body={macro.body}")
                .withName(new I18nProperty(SHORT_BODY_MACRO_NAME, ""))
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        DynamicContentMacroModuleBean parameterMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-macro")
                .withName(new I18nProperty(PARAMETER_MACRO_NAME, ""))
                .withParameters(newMacroParameterBean()
                        .withIdentifier("param1")
                        .withName(new I18nProperty("Param 1", ""))
                        .withType("string")
                        .build()
                )
                .build();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapabilities("dynamicContentMacros",
                        simpleMacro,
                        allParameterTypesMacro,
                        longBodyMacro,
                        shortBodyMacro,
                        featuredMacro,
                        imagePlaceholderMacro,
                        parameterMacro
                )
                .addRoute("/render-macro", ConnectAppServlets.helloWorldServlet())
                .addRoute("/images/placeholder.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .start();

        overridePageObjects();
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
    public void testMacroIsRendered() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Simple Macro on Page");

        selectSimpleMacro(editorPage);

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(SIMPLE_MACRO_KEY, 0);
        String content = renderedMacro.getIFrameElementText("hello-world-message");
        rpc.removePage(savedPage.getPageId());

        assertThat(content, is("Hello world"));
    }

    @Test
    public void testBodyInclusion() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Short Body Macro");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        macroBrowser.selectAndInsertMacro(SHORT_BODY_MACRO_KEY);

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        editorContent.setMacroBody("a short body", true);

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(SHORT_BODY_MACRO_KEY, 0);
        String body = renderedMacro.getFromQueryString("body");
        rpc.removePage(savedPage.getPageId());

        assertThat(body, is("<p>a short body</p>"));
    }

    @Test
    public void testBodyHashInclusion() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Long Body Macro");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        macroBrowser.selectAndInsertMacro(LONG_BODY_MACRO_KEY);

        String body = StringUtils.repeat("x ", 200);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        editorContent.setMacroBody(body, false);

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(LONG_BODY_MACRO_KEY, 0);
        String hash = renderedMacro.getFromQueryString("hash");
        rpc.removePage(savedPage.getPageId());

        assertThat(hash, is(DigestUtils.md5Hex(body)));
    }

    @Test
    public void testParameterInclusion() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Parameter Page");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(PARAMETER_MACRO_NAME);
        ConfluenceMacroForm macroForm = (ConfluenceMacroForm) macro.select();

        macroForm.getAutocompleteField("param1").setValue("param value");
        macroBrowser.insertMacro();

        ViewPage savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(PARAMETER_MACRO_KEY, 0);
        String value = renderedMacro.getFromQueryString("param1");

        rpc.removePage(savedPage.getPageId());

        assertThat(value, is("param value"));
    }

    @Test
    public void testMultipleMacrosOnPage() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Multiple Macros");

        selectSimpleMacro(editorPage);
        selectSimpleMacro(editorPage);

        ViewPage savedPage = editorPage.save();

        RenderedMacro renderedMacro1 = connectPageOperations.findMacro(SIMPLE_MACRO_KEY, 0);
        String content1 = renderedMacro1.getIFrameElementText("hello-world-message");

        RenderedMacro renderedMacro2 = connectPageOperations.findMacro(SIMPLE_MACRO_KEY, 1);
        String content2 = renderedMacro2.getIFrameElementText("hello-world-message");

        rpc.removePage(savedPage.getPageId());

        assertThat(content1, is(content2));
    }

    @Override
    protected String getAddonBaseUrl()
    {
        return remotePlugin.getAddon().getBaseUrl();
    }

    private void selectSimpleMacro(CreatePage editorPage)
    {
        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        macroBrowser.selectAndInsertMacro(SIMPLE_MACRO_KEY);
    }
}
