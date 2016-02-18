package it.confluence.macro;

import com.atlassian.confluence.it.Page;
import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.page.content.EditorPage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MatcherBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.confluence.ConfluenceWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean.newStaticContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static com.atlassian.plugin.connect.test.common.util.AddonTestUtils.randomAddonKey;
import static com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor.toConfluenceUser;
import static it.confluence.ConfluenceWebDriverTestBase.TestSpace.DEMO;
import static it.confluence.servlet.ConfluenceAppServlets.dynamicMacroStaticServlet;
import static org.junit.Assert.assertEquals;

/**
 * Integration tests that create a Connect Addon that uses the autoconvert feature and exercise its usage in Confluence pages.
 */
public class TestAutoconvert extends ConfluenceWebDriverTestBase
{
    private static final Logger logger = LoggerFactory.getLogger(TestAutoconvert.class);
    private static final String DYNAMIC_MACRO_WITH_AUTOCONVERT = "Dynamic Macro With Autoconvert";
    private static final String STATIC_MACRO_WITH_AUTOCONVERT = "Static Macro With Autoconvert";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        DynamicContentMacroModuleBean dynamicMacroWithAutoconvert = newDynamicContentMacroModuleBean()
                .withUrl("/dynamic-macro?url={url}")
                .withKey("dynamic-macro-with-autoconvert")
                .withName(new I18nProperty(DYNAMIC_MACRO_WITH_AUTOCONVERT, null))
                .withParameters(
                        newMacroParameterBean()
                                .withIdentifier("url")
                                .withName(new I18nProperty("URL", null))
                                .withType("string")
                                .build())
                .withAutoconvert(AutoconvertBean.newAutoconvertBean()
                        .withUrlParameter("url")
                        .withMatchers(
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://google.com/dynamic")
                                        .build(),
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://google.com/dynamic/{}")
                                        .build())
                        .build())
                .build();

        StaticContentMacroModuleBean staticMacroWithAutoconvert = newStaticContentMacroModuleBean()
                .withUrl("/static-macro?url={url}")
                .withKey("static-macro-with-autoconvert")
                .withName(new I18nProperty(STATIC_MACRO_WITH_AUTOCONVERT, null))
                .withParameters(
                        newMacroParameterBean()
                                .withIdentifier("url")
                                .withName(new I18nProperty("URL", null))
                                .withType("string")
                                .build())
                .withAutoconvert(AutoconvertBean.newAutoconvertBean()
                        .withUrlParameter("url")
                        .withMatchers(
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://google.com/static")
                                        .build(),
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://google.com/static/{}")
                                        .build())
                        .build())
                .build();

        remotePlugin = new ConnectRunner(getProduct().getProductInstance().getBaseUrl(), randomAddonKey())
                .setAuthenticationToNone()
                .addScope(ScopeName.ADMIN) // for using ap.request
                .addModules("dynamicContentMacros", dynamicMacroWithAutoconvert)
                .addModules("staticContentMacros", staticMacroWithAutoconvert)
                .addRoute("/dynamic-macro", ConnectAppServlets.helloWorldServlet())
                .addRoute("/static-macro", dynamicMacroStaticServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    /*
        Create page matching tests
     */

    @Test
    public void testAutoconvertDynamicMacroOnCreate() throws Exception
    {
        EditorPage editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndMatch(editorPage, "dynamic-macro-with-autoconvert", "https://google.com/dynamic");
    }

    @Test
    public void testAutoconvertDynamicMacroWithTokenOnCreate() throws Exception
    {
        EditorPage editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndMatch(editorPage, "dynamic-macro-with-autoconvert", "https://google.com/dynamic/variable");
    }

    @Test
    public void testAutoconvertStaticMacroOnCreate() throws Exception
    {
        EditorPage editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndMatch(editorPage, "static-macro-with-autoconvert", "https://google.com/static");
    }

    @Test
    public void testAutoconvertStaticMacroWithTokenOnCreate() throws Exception
    {
        EditorPage editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndMatch(editorPage, "static-macro-with-autoconvert", "https://google.com/static/variable");
    }

    /*
        Edit page matching tests
     */

    @Test
    public void testAutoconvertDynamicMacroOnEdit() throws Exception
    {
        EditorPage editorPage = getNewSavedPageForEditing(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndMatch(editorPage, "dynamic-macro-with-autoconvert", "https://google.com/dynamic");
    }

    @Test
    public void testAutoconvertDynamicMacroWithTokenOnEdit() throws Exception
    {
        EditorPage editorPage = getNewSavedPageForEditing(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndMatch(editorPage, "dynamic-macro-with-autoconvert", "https://google.com/dynamic/variable");
    }

    @Test
    public void testAutoconvertStaticMacroOnEdit() throws Exception
    {
        EditorPage editorPage = getNewSavedPageForEditing(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndMatch(editorPage, "static-macro-with-autoconvert", "https://google.com/static");
    }

    @Test
    public void testAutoconvertStaticMacroWithTokenOnEdit() throws Exception
    {
        EditorPage editorPage = getNewSavedPageForEditing(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndMatch(editorPage, "static-macro-with-autoconvert", "https://google.com/static/variable");
    }

    /*
        Non-matching tests
     */

    @Test
    public void testAutoconvertPrefixNoMatch() throws Exception
    {
        EditorPage editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndNoMatch(editorPage, "WontMatchhttps://google.com");
    }

    @Test
    public void testAutoconvertSuffixNoMatch() throws Exception
    {
        EditorPage editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);
        pasteLinkAndNoMatch(editorPage, "https://google.comwontmatch");
    }

    /*
        Testing helper methods
     */

    public EditorPage getNewSavedPageForEditing(User user, Space space)
    {
        String title = "Test page " + System.currentTimeMillis();
        Page page = new Page(space, title, "I'm some testing content!");
        rpc.createPage(page);
        ViewPage viewPage = getProduct().loginAndView(user, page);
        EditorPage editorPage = viewPage.edit();
        return editorPage;
    }

    private String pasteLinkAndSave(EditorPage editorPage, Option<String> macroName, String link)
    {
        EditorContent editorContent = editorPage.getEditor().getContent();
        editorContent.focus();
        editorContent.pasteContent(link);
        editorPage.setTitle("TestAutoconvert-" + System.currentTimeMillis());

        // if the macro name is specified we need to wait for the autoconvert to complete
        if (macroName.isDefined())
        {
            TimedQuery<Boolean> query = editorContent.isElementPresentInEditorContentTimed(By.cssSelector("img[data-macro-name='" + macroName.get() + "']"));
            Poller.waitUntilTrue("autoconvert round trip failed: "+editorContent.getTimedHtml().byDefaultTimeout(), query);
            logger.error("found image - content: " + editorContent.getTimedHtml().byDefaultTimeout());
        }

        ViewPage viewPage = editorPage.saveWithKeyboardShortcut();
        long pageId = viewPage.getPageId();
        return rpc.getPageContent(pageId);
    }

    private void pasteLinkAndMatch(EditorPage editorPage, String macroName, String link)
    {
        try
        {
            String pageContent = pasteLinkAndSave(editorPage, Option.some(macroName), link);
            Document doc = Jsoup.parse(pageContent, "", Parser.xmlParser());
            // check that the macro was created correctly
            Elements elements = doc.select("ac|structured-macro");
            assertEquals(1, elements.size());
            Element macroElement = elements.get(0);
            assertEquals(macroName, macroElement.attr("ac:name"));
            Elements parameterElements = macroElement.select("ac|parameter");
            assertEquals(1, parameterElements.size());
            Element urlParameter = parameterElements.get(0);
            assertEquals(link, urlParameter.text());
        }
        finally
        {
            cancelEditor(editorPage);
        }

    }

    private void pasteLinkAndNoMatch(EditorPage editorPage, String link)
    {
        String pageContent = pasteLinkAndSave(editorPage, Option.<String>none(), link);
        Document doc = Jsoup.parse(pageContent, "", Parser.xmlParser());
        // check that the macro was created correctly
        Elements elements = doc.select("ac|structured-macro");
        assertEquals(0, elements.size());
    }
}
