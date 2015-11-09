package it.confluence.macro;

import java.util.Map;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.HttpUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import com.google.common.collect.Maps;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.MacroStorageFormatBuilder;
import it.confluence.servlet.macro.BodyHandler;
import it.confluence.servlet.macro.MacroBodyServlet;
import it.servlet.HttpContextServlet;
import it.servlet.InstallHandlerServlet;
import junit.framework.TestCase;

/**
 * This test case will collect the macro body from confluence in all the different ways possible.  It will check
 * both static and dynamic macros being collected by hash or id.
 */
public class TestMacroBody extends ConfluenceWebDriverTestBase
{
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        DynamicContentMacroModuleBean dynamicContentMacroById = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/render-dynamic-by-id?pageId={page.id}&pageVersion={page.version}&macroId={macro.id}")
                .withDescription(new I18nProperty("Dynamic Content Macro By Id", null))
                .withKey("dynamic-macro-by-id")
                .withName(new I18nProperty("Dynamic Macro By Id", null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        StaticContentMacroModuleBean staticContentMacroById = StaticContentMacroModuleBean.newStaticContentMacroModuleBean()
                .withUrl("/render-static-by-id?pageId={page.id}&pageVersion={page.version}&macroId={macro.id}")
                .withDescription(new I18nProperty("Static Content Macro By Id", null))
                .withKey("static-macro-by-id")
                .withName(new I18nProperty("Static Macro By Id", null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        DynamicContentMacroModuleBean dynamicContentMacroByHash = DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                .withUrl("/render-dynamic-by-hash?pageId={page.id}&pageVersion={page.version}&macroHash={macro.hash}")
                .withDescription(new I18nProperty("Dynamic Content Macro By Hash", null))
                .withKey("dynamic-macro-by-hash")
                .withName(new I18nProperty("Dynamic Macro By Hash", null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        StaticContentMacroModuleBean staticContentMacroByHash = StaticContentMacroModuleBean.newStaticContentMacroModuleBean()
                .withUrl("/render-static-by-hash?pageId={page.id}&pageVersion={page.version}&macroHash={macro.hash}")
                .withDescription(new I18nProperty("Static Content Macro By Hash", null))
                .withKey("static-macro-by-hash")
                .withName(new I18nProperty("Static Macro By Hash", null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        final InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
        String addonKey = AddonTestUtils.randomAddOnKey();
        String baseUrl = product.getProductInstance().getBaseUrl();
        BodyHandler dynamicMacroBodyHandler = (req, resp, context, body) -> {
            Map<String, Object> data = Maps.newHashMap(context);
            data.put("body", body);
            HttpUtils.renderHtml(resp, "it/confluence/macro/dynamic-macro-body.mu", data);
        };
        BodyHandler staticMacroBodyHandler = (req, resp, context, body) -> {
            resp.setContentType("text/html");
            byte[] bytes = body.getBytes();
            resp.setContentLength(bytes.length);
            resp.getOutputStream().write(bytes);
            resp.getOutputStream().close();
        };
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .addJWT(installHandlerServlet)
                .addScope(ScopeName.ADMIN) // for using ap.request
                .addModules("dynamicContentMacros", dynamicContentMacroById, dynamicContentMacroByHash)
                .addModules("staticContentMacros", staticContentMacroById, staticContentMacroByHash)
                .addRoute("/installed", installHandlerServlet)
                .addRoute("/render-dynamic-by-id", new HttpContextServlet(new MacroBodyServlet(MacroBodyServlet.CollectionType.BY_ID, baseUrl, addonKey, installHandlerServlet, dynamicMacroBodyHandler)))
                .addRoute("/render-static-by-id", new HttpContextServlet(new MacroBodyServlet(MacroBodyServlet.CollectionType.BY_ID, baseUrl, addonKey, installHandlerServlet, staticMacroBodyHandler)))
                .addRoute("/render-dynamic-by-hash", new HttpContextServlet(new MacroBodyServlet(MacroBodyServlet.CollectionType.BY_HASH, baseUrl, addonKey, installHandlerServlet, dynamicMacroBodyHandler)))
                .addRoute("/render-static-by-hash", new HttpContextServlet(new MacroBodyServlet(MacroBodyServlet.CollectionType.BY_HASH, baseUrl, addonKey, installHandlerServlet, staticMacroBodyHandler)))
                .start();
    }

    @BeforeClass
    public static void logoutBeforeClass()
    {
        getProduct().logOutFast();
    }

    @After
    public void logoutAfter()
    {
        logoutBeforeClass();
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
    public void testDynamicMacroById() throws Exception
    {
        testDynamicMacro("dynamic-macro-by-id");
    }

    @Test
    public void testDynamicMacroByHash() throws Exception
    {
        testDynamicMacro("dynamic-macro-by-hash");
    }

    private void testDynamicMacro(final String macroKey) throws Exception
    {
        final Content page = createPageWithRichTextMacroAndBody(macroKey, "<h1>Hello world</h1>");
        ViewPage viewPage = getProduct().login(testUserFactory.basicUser().confUser(), ViewPage.class, String.valueOf(page.getId().asLong()));
        viewPage.getRenderedContent().getTextTimed().byDefaultTimeout();
        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(macroKey, 0);
        String content1 = renderedMacro.getIFrameElement("body");
        Assert.assertThat(content1, CoreMatchers.is("<h1>Hello world</h1>"));
    }

    @Test
    public void testStaticMacroById() throws Exception
    {
        testStaticMacro("static-macro-by-id");
    }

    @Test
    public void testStaticMacroByHash() throws Exception
    {
        testStaticMacro("static-macro-by-hash");
    }

    private void testStaticMacro(String macroKey)
    {
        login(testUserFactory.basicUser());
        String headingText = "Test Static Macro " + macroKey + "";
        Content page = createPageWithRichTextMacroAndBody(macroKey, "<h1>" + headingText + "</h1>");
        ViewPage viewPage = getProduct().viewPage(String.valueOf(page.getId().asLong()));
        String headingTextFromPage = viewPage.getMainContent().find(By.tagName("h1")).getText();
        TestCase.assertEquals(headingText, headingTextFromPage);
    }

    private Content createPageWithRichTextMacroAndBody(String macroKey, String macroBody)
    {
        long tag = System.currentTimeMillis();
        String contentBody = new MacroStorageFormatBuilder(macroKey).richTextBody(macroBody).build();
        return createPage("test page - " + tag, contentBody);
    }
}
