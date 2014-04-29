package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import it.servlet.EchoContextServlet;
import it.servlet.EchoQueryParametersServlet;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestStaticContentMacro extends AbstractContentMacroTest
{
    private static final String STORAGE_FORMAT_MACRO_NAME = "Storage Format Macro";
    private static final String STORAGE_FORMAT_MACRO_KEY = "storage-format-macro";

    private static final String GET_MACRO_NAME = "Get Macro";
    private static final String GET_MACRO_KEY = "get-macro";

    private static ConnectRunner remotePlugin;
    private static EchoQueryParametersServlet parameterServlet;
    private static EchoContextServlet contextServlet;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        StaticContentMacroModuleBean simpleMacro = createSimpleMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean allParameterTypesMacro = createAllParametersMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean featuredMacro = createFeaturedMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean imagePlaceholderMacro = createImagePlaceholderMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean longBodyMacro = createLongBodyMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean shortBodyMacro = createShortBodyMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean parameterMacro = createParameterMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean editorMacro = createEditorMacro(newStaticContentMacroModuleBean());
        StaticContentMacroModuleBean hiddenMacro = createHiddenMacro(newStaticContentMacroModuleBean());

        StaticContentMacroModuleBean storageFormatMacro = newStaticContentMacroModuleBean()
                .withUrl("/render-storage-format")
                .withKey(STORAGE_FORMAT_MACRO_KEY)
                .withName(new I18nProperty(STORAGE_FORMAT_MACRO_NAME, ""))
                .build();

        StaticContentMacroModuleBean getMacro = newStaticContentMacroModuleBean()
                .withUrl("/render-context")
                .withKey(GET_MACRO_KEY)
                .withName(new I18nProperty(GET_MACRO_NAME, ""))
                .build();

        parameterServlet = new EchoQueryParametersServlet();
        contextServlet = new EchoContextServlet();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addModules("staticContentMacros",
                        simpleMacro,
                        allParameterTypesMacro,
                        longBodyMacro,
                        shortBodyMacro,
                        featuredMacro,
                        imagePlaceholderMacro,
                        parameterMacro,
                        storageFormatMacro,
                        getMacro,
                        editorMacro,
                        hiddenMacro
                )
                .addRoute(DEFAULT_MACRO_URL, ConnectAppServlets.wrapContextAwareServlet(parameterServlet))
                .addRoute("/render-editor", ConnectAppServlets.macroEditor())
                .addRoute("/echo/params", ConnectAppServlets.echoQueryParametersServlet())
                .addRoute("/render-context", ConnectAppServlets.wrapContextAwareServlet(contextServlet))
                .addRoute("/images/placeholder.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
                .addRoute("/render-storage-format", ConnectAppServlets.resourceServlet("confluence/test-static-content-macro.xhtml", "application/xhtml+xml"))
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
    public void testMacroIsRendered() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Simple Macro on Page_" + System.currentTimeMillis());

        selectMacro(editorPage, STORAGE_FORMAT_MACRO_NAME);

        savedPage = editorPage.save();

        String content = savedPage.getRenderedContent().getText();

        assertThat(content, is("Storage Format Content"));
    }

    @Test
    public void testMacroHttpMethod() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("HTTP GET Macro_" + System.currentTimeMillis());

        selectMacro(editorPage, GET_MACRO_NAME);

        savedPage = editorPage.save();

        assertThat(String.valueOf(contextServlet.waitForContext().get("req_method")), is("GET"));
    }

    @Test
    public void testBodyInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Short Body Macro_" + System.currentTimeMillis());

        selectMacro(editorPage, SHORT_BODY_MACRO_NAME);

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getEditor().getContent();
        editorContent.setRichTextMacroBody("a short body");

        savedPage = editorPage.save();

        String body = parameterServlet.waitForQueryParameters().any("body").getValue();
        assertThat(body, is("<p>a short body</p>"));
    }

    @Test
    public void testBodyHashInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Long Body Macro_" + System.currentTimeMillis());

        selectMacro(editorPage, LONG_BODY_MACRO_NAME);

        String body = StringUtils.repeat("x ", 200);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getEditor().getContent();
        editorContent.setPlainTextMacroBody(body);

        savedPage = editorPage.save();

        String hash = parameterServlet.waitForQueryParameters().any("hash").getValue();
        assertThat(hash, is(DigestUtils.md5Hex(body)));
    }

    @Test
    public void testParameterInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Parameter Page_" + System.currentTimeMillis());

        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(PARAMETER_MACRO_NAME);
        MacroForm macroForm = macro.select();

        macroForm.getAutocompleteField("param1").setValue("param value");
        macroBrowser.clickSave();

        savedPage = editorPage.save();

        String value = parameterServlet.waitForQueryParameters().any("param1").getValue();
        assertThat(value, is("param value"));
    }

    @Override
    protected String getAddonBaseUrl()
    {
        return remotePlugin.getAddon().getBaseUrl();
    }

    @Override
    protected ConnectAddonBean getCurrentAddon()
    {
        return remotePlugin.getAddon();
    }
}
