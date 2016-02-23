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
import com.atlassian.plugin.connect.test.common.pageobjects.RenderedMacro;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.HttpContextServlet;
import com.atlassian.plugin.connect.test.common.servlet.HttpUtils;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import com.google.common.collect.Maps;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.MacroStorageFormatBuilder;
import it.confluence.servlet.macro.BodyHandler;
import it.confluence.servlet.macro.MacroBodyServlet;
import org.openqa.selenium.WebElement;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor.toConfluenceUser;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * This test case will collect the macro body from confluence in all the different ways possible.  It will check
 * both static and dynamic macros being collected by hash or id.
 */
public class TestMacroBody extends ConfluenceWebDriverTestBase {
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        DynamicContentMacroModuleBean dynamicContentMacroById = getDynamicContentMacroModuleBean("dynamic-macro-by-id");
        DynamicContentMacroModuleBean dynamicContentMacroById1 = getDynamicContentMacroModuleBean("dynamic-macro-by-id-1");
        DynamicContentMacroModuleBean dynamicContentMacroById2 = getDynamicContentMacroModuleBean("dynamic-macro-by-id-2");
        DynamicContentMacroModuleBean dynamicContentMacroById3 = getDynamicContentMacroModuleBean("dynamic-macro-by-id-3");

        StaticContentMacroModuleBean staticContentMacroById = getStaticContentMacroModuleBean("static-macro-by-id");
        StaticContentMacroModuleBean staticContentMacroById1 = getStaticContentMacroModuleBean("static-macro-by-id-1");
        StaticContentMacroModuleBean staticContentMacroById2 = getStaticContentMacroModuleBean("static-macro-by-id-2");
        StaticContentMacroModuleBean staticContentMacroById3 = getStaticContentMacroModuleBean("static-macro-by-id-3");

        DynamicContentMacroModuleBean dynamicContentMacroByHash = newDynamicContentMacroModuleBean()
                .withUrl("/render-dynamic-by-hash?pageId={page.id}&pageVersion={page.version}&macroHash={macro.hash}")
                .withDescription(new I18nProperty("Dynamic Content Macro By Hash", null))
                .withKey("dynamic-macro-by-hash")
                .withName(new I18nProperty("Dynamic Macro By Hash", null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        StaticContentMacroModuleBean staticContentMacroByHash = newStaticContentMacroModuleBean()
                .withUrl("/render-static-by-hash?pageId={page.id}&pageVersion={page.version}&macroHash={macro.hash}")
                .withDescription(new I18nProperty("Static Content Macro By Hash", null))
                .withKey("static-macro-by-hash")
                .withName(new I18nProperty("Static Macro By Hash", null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        final InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
        String addonKey = AddonTestUtils.randomAddonKey();
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
                .addModules("dynamicContentMacros", dynamicContentMacroById, dynamicContentMacroByHash, dynamicContentMacroById1, dynamicContentMacroById2, dynamicContentMacroById3)
                .addModules("staticContentMacros", staticContentMacroById, staticContentMacroByHash, staticContentMacroById1, staticContentMacroById2, staticContentMacroById3)
                .addRoute("/installed", installHandlerServlet)
                .addRoute("/render-dynamic-by-id", new HttpContextServlet(new MacroBodyServlet(MacroBodyServlet.CollectionType.BY_ID, baseUrl, addonKey, installHandlerServlet, dynamicMacroBodyHandler)))
                .addRoute("/render-static-by-id", new HttpContextServlet(new MacroBodyServlet(MacroBodyServlet.CollectionType.BY_ID, baseUrl, addonKey, installHandlerServlet, staticMacroBodyHandler)))
                .addRoute("/render-dynamic-by-hash", new HttpContextServlet(new MacroBodyServlet(MacroBodyServlet.CollectionType.BY_HASH, baseUrl, addonKey, installHandlerServlet, dynamicMacroBodyHandler)))
                .addRoute("/render-static-by-hash", new HttpContextServlet(new MacroBodyServlet(MacroBodyServlet.CollectionType.BY_HASH, baseUrl, addonKey, installHandlerServlet, staticMacroBodyHandler)))
                .start();
    }

    private static StaticContentMacroModuleBean getStaticContentMacroModuleBean(String key) {
        return newStaticContentMacroModuleBean()
                .withUrl("/render-static-by-id?pageId={page.id}&pageVersion={page.version}&macroId={macro.id}")
                .withDescription(new I18nProperty("Static Content Macro By Id", null))
                .withKey(key)
                .withName(new I18nProperty("Static Macro By Id", null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();
    }

    private static DynamicContentMacroModuleBean getDynamicContentMacroModuleBean(String key) {
        return newDynamicContentMacroModuleBean()
                .withUrl("/render-dynamic-by-id?pageId={page.id}&pageVersion={page.version}&macroId={macro.id}")
                .withDescription(new I18nProperty("Dynamic Content Macro By Id", null))
                .withKey(key)
                .withName(new I18nProperty("Dynamic Macro By Id", null))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();
    }

    @BeforeClass
    public static void logoutBeforeClass() {
        getProduct().logOutFast();
    }

    @After
    public void logoutAfter() {
        logoutBeforeClass();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testDynamicMacroById() throws Exception {
        testDynamicMacro("dynamic-macro-by-id");
    }

    @Test
    public void testDynamicMacroByHash() throws Exception {
        testDynamicMacro("dynamic-macro-by-hash");
    }

    private void testDynamicMacro(final String macroKey) throws Exception {
        final Content page = createPageWithRichTextMacroAndBody(macroKey, "<h1>Hello world</h1>");
        ViewPage viewPage = getProduct().login(toConfluenceUser(testUserFactory.basicUser()), ViewPage.class, valueOf(page.getId().asLong()));
        viewPage.getRenderedContent().getTextTimed().byDefaultTimeout();
        RenderedMacro renderedMacro = confluencePageOperations.findMacroWithIdPrefix(macroKey, 0);
        String content1 = renderedMacro.getIFrameElement("body");
        assertThat(content1, CoreMatchers.is("<h1>Hello world</h1>"));
    }

    private void testNestedDynamicMacro(final String... macroKeys) throws Exception {
        final Content page = makePageFromContentBody(makeNestedContentBody(macroKeys, "<h1>Hello world</h1>"));
        ViewPage viewPage = getProduct().login(toConfluenceUser(testUserFactory.basicUser()), ViewPage.class, valueOf(page.getId().asLong()));
        viewPage.getRenderedContent().getTextTimed().byDefaultTimeout();
        RenderedMacro renderedMacro = confluencePageOperations.findMacroWithIdPrefix(macroKeys[macroKeys.length - 1], 0);
        String content1 = renderedMacro.getIFrameElement("body");
        assertThat(content1, CoreMatchers.containsString("Hello world"));
    }

    @Test
    public void testStaticMacroById() throws Exception {
        testStaticMacro("static-macro-by-id");
    }

    @Test
    public void testStaticMacroByHash() throws Exception {
        testStaticMacro("static-macro-by-hash");
    }

    private void testStaticMacro(String macroKey) {
        login(testUserFactory.basicUser());
        String headingText = "Test Static Macro " + macroKey + "";
        Content page = createPageWithRichTextMacroAndBody(macroKey, "<h1>" + headingText + "</h1>");
        ViewPage viewPage = getProduct().viewPage(String.valueOf(page.getId().asLong()));
        String headingTextFromPage = viewPage.getMainContent().find(By.tagName("h1")).getText();
        assertEquals(headingText, headingTextFromPage);
    }

    private void testNestedStaticMacro(final String... macroKeys) {
        login(testUserFactory.basicUser());
        Content page = makePageFromContentBody(makeNestedContentBody(macroKeys, "<h1 class=\"hello-world-content\">Hello world</h1>"));
        ViewPage viewPage = getProduct().viewPage(String.valueOf(page.getId().asLong()));

        viewPage.getRenderedContent().getTextTimed().byDefaultTimeout();
        WebElement element = confluencePageOperations.findElementByClass("hello-world-content");
        assertThat(element.getText(), CoreMatchers.is("Hello world"));
    }

    private Content createPageWithRichTextMacroAndBody(String macroKey, String macroBody) {
        String contentBody = makeContentBody(macroKey, macroBody);
        return makePageFromContentBody(contentBody);
    }

    private Content makePageFromContentBody(String contentBody) {
        long tag = currentTimeMillis();
        return createPage("test page - " + tag, contentBody);
    }

    private String makeContentBody(String macroKey, String macroBody) {
        return new MacroStorageFormatBuilder(macroKey).richTextBody(macroBody).build();
    }

    private String makeNestedContentBody(String[] macroKeys, String innerBody) {
        String body = makeContentBody(macroKeys[0], innerBody);
        for (int i = 1; i < macroKeys.length; i++) {
            body = makeContentBody(macroKeys[i], body);
        }

        return body;
    }

    @Test
    public void testNestedDynamicMacroById() throws Exception {
        testNestedDynamicMacro("dynamic-macro-by-id-1", "dynamic-macro-by-id-2", "dynamic-macro-by-id-3");
    }

    @Test
    public void testNestedStaticMacroById() throws Exception {
        testNestedStaticMacro("static-macro-by-id-1", "static-macro-by-id-2", "static-macro-by-id-3");
    }
}
