package it.modules.confluence;

import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.it.Page;
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
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import com.google.common.collect.Maps;
import it.servlet.HttpContextServlet;
import it.servlet.InstallHandlerServlet;
import it.servlet.macro.BodyHandler;
import it.servlet.macro.MacroBodyServlet;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static junit.framework.TestCase.assertEquals;

/**
 * This test case will collect the macro body from confluence in all the different ways possible.  It will check
 * both static and dynamic macros being collected by hash or id.
 */
public class TestMacroBody extends AbstractConfluenceWebDriverTest
{
    private static final Logger logger = LoggerFactory.getLogger(TestMacroBody.class);

    @Rule public WebDriverScreenshotRule webDriverScreenshotRule = new WebDriverScreenshotRule();
    @Rule public Timeout globalTimeout = new Timeout(23000);

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        DynamicContentMacroModuleBean dynamicContentMacroById = newDynamicContentMacroModuleBean()
                .withUrl("/render-dynamic-by-id?pageId={page.id}&pageVersion={page.version}&macroId={macro.id}")
                .withDescription(new I18nProperty("Dynamic Content Macro By Id", ""))
                .withKey("dynamic-macro-by-id")
                .withName(new I18nProperty("Dynamic Macro By Id", ""))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        StaticContentMacroModuleBean staticContentMacroById = newStaticContentMacroModuleBean()
                .withUrl("/render-static-by-id?pageId={page.id}&pageVersion={page.version}&macroId={macro.id}")
                .withDescription(new I18nProperty("Static Content Macro By Id", ""))
                .withKey("static-macro-by-id")
                .withName(new I18nProperty("Static Macro By Id", ""))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        DynamicContentMacroModuleBean dynamicContentMacroByHash = newDynamicContentMacroModuleBean()
                .withUrl("/render-dynamic-by-hash?pageId={page.id}&pageVersion={page.version}&macroHash={macro.hash}")
                .withDescription(new I18nProperty("Dynamic Content Macro By Hash", ""))
                .withKey("dynamic-macro-by-hash")
                .withName(new I18nProperty("Dynamic Macro By Hash", ""))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        StaticContentMacroModuleBean staticContentMacroByHash = newStaticContentMacroModuleBean()
                .withUrl("/render-static-by-hash?pageId={page.id}&pageVersion={page.version}&macroHash={macro.hash}")
                .withDescription(new I18nProperty("Static Content Macro By Hash", ""))
                .withKey("static-macro-by-hash")
                .withName(new I18nProperty("Static Macro By Hash", ""))
                .withOutputType(MacroOutputType.BLOCK)
                .withBodyType(MacroBodyType.RICH_TEXT)
                .build();

        final InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
        String addonKey = AddonTestUtils.randomAddOnKey();
        String baseUrl = product.getProductInstance().getBaseUrl();
        BodyHandler dynamicMacroBodyHandler = new BodyHandler()
        {
            public void processBody(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context, String body) throws IOException
            {
                Map<String, Object> data = Maps.newHashMap(context);
                data.put("body", body);
                HttpUtils.renderHtml(resp, "confluence/macro/dynamic-macro-body.mu", data);
            }
        };
        BodyHandler staticMacroBodyHandler = new BodyHandler()
        {
            public void processBody(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context, String body) throws IOException
            {
                resp.setContentType("text/html");
                byte[] bytes = body.getBytes();
                resp.setContentLength(bytes.length);
                resp.getOutputStream().write(bytes);
                resp.getOutputStream().close();
            }
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

    private void testDynamicMacro(String macroKey)
    {
        String body = "<h1>Test Dynamic Macro: " + macroKey + "</h1>";
        Page page = createPage(macroKey, body);
        String pageId = rpc.content.createPage(page, ContentRepresentation.STORAGE).getIdAsString();
        ViewPage viewPage = getProduct().login(TestUser.ADMIN.confUser(), ViewPage.class, pageId);

        for (int i = 0; i < 20; i++)
        {
            try
            {
                waitUntilTrue(viewPage.getMainContent().find(By.tagName("iframe")).timed().isPresent());
                logger.error("attempt number: "+(i)+": SUCCESS");
            }
            catch (Error e)
            {
                logger.error("attempt number: "+(i)+": FAILED");
                e.printStackTrace();
            }
        }

        RenderedMacro renderedMacro = connectPageOperations.findMacroWithIdPrefix(macroKey);
        String bodyFromPage = renderedMacro.getIFrameElement("body");
        assertEquals(body, bodyFromPage);
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
        String headingText = "Test Static Macro " + macroKey + "";
        Page page = createPage(macroKey, "<h1>" + headingText + "</h1>");
        String pageId = rpc.content.createPage(page, ContentRepresentation.STORAGE).getIdAsString();
        ViewPage viewPage = getProduct().login(TestUser.ADMIN.confUser(), ViewPage.class, pageId);

        // we can't assert on the html here because confluence will have transformed it
        // so we assert on the contents of the h1 and look for the string we put in to the body
        String headingTextFromPage = viewPage.getMainContent().find(By.tagName("h1")).getText();
        assertEquals(headingText, headingTextFromPage);
    }

    private Page createPage(String macroKey, String body)
    {
        long tag = System.currentTimeMillis();
        String bodyWrapper = "<ac:rich-text-body>" + body + "</ac:rich-text-body>";
        String storageFormat = "<ac:structured-macro ac:name=\"" + macroKey + "\">" + bodyWrapper + "</ac:structured-macro>";
        return new Page(TestSpace.DEMO, "test page - " + tag, storageFormat);
    }
}
