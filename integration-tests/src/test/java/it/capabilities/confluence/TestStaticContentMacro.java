package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroHttpMethod;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroBrowserDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroForm;
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
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestStaticContentMacro extends AbstractContentMacroTest
{
    private static final String STORAGE_FORMAT_MACRO_NAME = "Storage Format Macro";
    private static final String STORAGE_FORMAT_MACRO_KEY = "storage-format-macro";

    private static final String POST_MACRO_NAME = "Post Macro";
    private static final String POST_MACRO_KEY = "post-macro";

    private static final String POST_PARAM_MACRO_NAME = "Post Parameter Macro";
    private static final String POST_PARAM_MACRO_KEY = "post-parameter-macro";

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

        StaticContentMacroModuleBean storageFormatMacro = newStaticContentMacroModuleBean()
                .withKey(STORAGE_FORMAT_MACRO_KEY)
                .withUrl("/render-storage-format")
                .withName(new I18nProperty(STORAGE_FORMAT_MACRO_NAME, ""))
                .build();

        StaticContentMacroModuleBean postMacro = newStaticContentMacroModuleBean()
                .withKey(POST_MACRO_KEY)
                .withUrl("/render-context")
                .withMethod(MacroHttpMethod.POST)
                .withName(new I18nProperty(POST_MACRO_NAME, ""))
                .build();

        StaticContentMacroModuleBean postParameterMacro = newStaticContentMacroModuleBean()
                .withUrl("/render-macro")
                .withKey(POST_PARAM_MACRO_KEY)
                .withName(new I18nProperty(POST_PARAM_MACRO_NAME, ""))
                .withMethod(MacroHttpMethod.POST)
                .withParameters(newMacroParameterBean()
                        .withIdentifier("param1")
                        .withName(new I18nProperty("Param 1", ""))
                        .withType("string")
                        .build()
                )
                .build();

        parameterServlet = new EchoQueryParametersServlet();
        contextServlet = new EchoContextServlet();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapabilities("staticContentMacros",
                        simpleMacro,
                        allParameterTypesMacro,
                        longBodyMacro,
                        shortBodyMacro,
                        featuredMacro,
                        imagePlaceholderMacro,
                        parameterMacro,
                        storageFormatMacro,
                        postMacro,
                        postParameterMacro
                )
                .addRoute("/render-macro", ConnectAppServlets.wrapContextAwareServlet(parameterServlet))
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
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Simple Macro on Page");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        macroBrowser.selectAndInsertMacro(STORAGE_FORMAT_MACRO_KEY);

        savedPage = editorPage.save();

        String content = savedPage.getRenderedContent().getText();

        assertThat(content, is("Storage Format Content"));
    }

    @Test
    public void testMacroHttpMethod() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("HTTP POST Macro");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        macroBrowser.selectAndInsertMacro(POST_MACRO_KEY);

        savedPage = editorPage.save();

        assertThat(String.valueOf(contextServlet.waitForContext().get("req_method")), is("POST"));
    }

    @Test
    public void testBodyInclusion() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Short Body Macro");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        macroBrowser.selectAndInsertMacro(SHORT_BODY_MACRO_KEY);

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        editorContent.setRichTextMacroBody("a short body");

        savedPage = editorPage.save();

        String body = parameterServlet.waitForQueryParameters().any("body").getValue();
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
        editorContent.setPlainTextMacroBody(body);

        savedPage = editorPage.save();

        String hash = parameterServlet.waitForQueryParameters().any("hash").getValue();
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

        savedPage = editorPage.save();

        String value = parameterServlet.waitForQueryParameters().any("param1").getValue();
        assertThat(value, is("param value"));
    }

    @Test
    public void testPOSTParameterInclusion() throws Exception
    {
        CreatePage editorPage = product.loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("HTTP POST Parameter Page");

        ConfluenceMacroBrowserDialog macroBrowser = (ConfluenceMacroBrowserDialog) editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(POST_PARAM_MACRO_NAME);
        ConfluenceMacroForm macroForm = (ConfluenceMacroForm) macro.select();

        macroForm.getAutocompleteField("param1").setValue("param value");
        macroBrowser.insertMacro();

        savedPage = editorPage.save();

        String value = parameterServlet.waitForQueryParameters().any("param1").getValue();
        assertThat(value, is("param value"));
    }

    @Override
    protected String getAddonBaseUrl()
    {
        return remotePlugin.getAddon().getBaseUrl();
    }
}
