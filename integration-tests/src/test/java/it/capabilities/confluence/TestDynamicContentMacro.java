package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditorContent;
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
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;

public class TestDynamicContentMacro extends AbstractContentMacroTest
{
    private static final String SMALL_INLINE_MACRO_NAME = "Small Inline Macro";
    private static final String SMALL_INLINE_MACRO_KEY = "small-inline-macro";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        DynamicContentMacroModuleBean simpleMacro = createSimpleMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean allParameterTypesMacro = createAllParametersMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean featuredMacro = createFeaturedMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean imagePlaceholderMacro = createImagePlaceholderMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean longBodyMacro = createLongBodyMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean shortBodyMacro = createShortBodyMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean parameterMacro = createParameterMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean editorMacro = createEditorMacro(newDynamicContentMacroModuleBean());

        DynamicContentMacroModuleBean smallInlineMacro = newDynamicContentMacroModuleBean()
                .withUrl("/render-no-resize-macro")
                .withName(new I18nProperty(SMALL_INLINE_MACRO_NAME, ""))
                .withOutputType(MacroOutputType.INLINE)
                .withWidth("60px")
                .withHeight("30px")
                .build();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapabilities("dynamicContentMacros",
                        simpleMacro,
                        allParameterTypesMacro,
                        longBodyMacro,
                        shortBodyMacro,
                        featuredMacro,
                        imagePlaceholderMacro,
                        parameterMacro,
                        smallInlineMacro,
                        editorMacro
                )
                .addRoute(DEFAULT_MACRO_URL, ConnectAppServlets.helloWorldServlet())
                .addRoute("/render-editor", ConnectAppServlets.helloWorldServlet())
                .addRoute("/render-no-resize-macro", ConnectAppServlets.noResizeServlet())
                .addRoute("/images/placeholder.png", ConnectAppServlets.resourceServlet("atlassian-icon-16.png", "image/png"))
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
        editorPage.setTitle("Simple Macro on Page");

        selectMacro(editorPage, SIMPLE_MACRO_NAME);

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(SIMPLE_MACRO_KEY, 0);
        String content = renderedMacro.getIFrameElementText("hello-world-message");

        assertThat(content, is("Hello world"));
    }

    @Test
    public void testBodyInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Short Body Macro");

        selectMacro(editorPage, SHORT_BODY_MACRO_NAME);

        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        editorContent.setRichTextMacroBody("a short body");

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(SHORT_BODY_MACRO_KEY, 0);
        String body = renderedMacro.getFromQueryString("body");

        assertThat(body, is("<p>a short body</p>"));
    }

    @Test
    public void testBodyHashInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Long Body Macro");

        selectMacro(editorPage, LONG_BODY_MACRO_NAME);

        String body = StringUtils.repeat("x ", 200);
        ConfluenceEditorContent editorContent = (ConfluenceEditorContent) editorPage.getContent();
        editorContent.setPlainTextMacroBody(body);

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(LONG_BODY_MACRO_KEY, 0);
        String hash = renderedMacro.getFromQueryString("hash");

        assertThat(hash, is(DigestUtils.md5Hex(body)));
    }

    @Test
    public void testParameterInclusion() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Parameter Page");

        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        MacroItem macro = macroBrowser.searchForFirst(PARAMETER_MACRO_NAME);
        ConfluenceMacroForm macroForm = (ConfluenceMacroForm) macro.select();

        macroForm.getAutocompleteField("param1").setValue("param value");
        macroBrowser.clickSave();

        savedPage = editorPage.save();
        RenderedMacro renderedMacro = connectPageOperations.findMacro(PARAMETER_MACRO_KEY, 0);
        String value = renderedMacro.getFromQueryString("param1");

        assertThat(value, is("param value"));
    }

    @Test
    public void testMultipleMacrosOnPage() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Multiple Macros");

        selectMacro(editorPage, SIMPLE_MACRO_NAME);
        selectMacro(editorPage, SIMPLE_MACRO_NAME);

        savedPage = editorPage.save();

        RenderedMacro renderedMacro1 = connectPageOperations.findMacro(SIMPLE_MACRO_KEY, 0);
        String content1 = renderedMacro1.getIFrameElementText("hello-world-message");

        RenderedMacro renderedMacro2 = connectPageOperations.findMacro(SIMPLE_MACRO_KEY, 1);
        String content2 = renderedMacro2.getIFrameElementText("hello-world-message");

        assertThat(content1, is(content2));
    }

    @Test
    public void testMacroDimensions() throws Exception
    {
        CreatePage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN, TestSpace.DEMO);
        editorPage.setTitle("Small Inline Macro");

        selectMacro(editorPage, SMALL_INLINE_MACRO_NAME);

        savedPage = editorPage.save();

        RenderedMacro renderedMacro = connectPageOperations.findMacro(SMALL_INLINE_MACRO_KEY, 0);

        assertThat(renderedMacro.getIFrameSize(), both(hasProperty("width", is(60))).and(hasProperty("height", is(30))));
    }


    @Override
    protected String getAddonBaseUrl()
    {
        return remotePlugin.getAddon().getBaseUrl();
    }
}
