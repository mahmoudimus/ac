package it.modules.confluence;

import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.page.content.EditorPage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MatcherBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.junit.Assert.assertEquals;

/**
 */
public class TestAutoconvert extends AbstractConfluenceWebDriverTest
{
    protected static final String OS_CTRL_KEY = "Mac OS X".equals(System.getProperty("os.name")) ? Keys.COMMAND.toString() : Keys.CONTROL.toString();
    private static final String DYNAMIC_MACRO_WITH_AUTOCONVERT = "Dynamic Macro With Autoconvert";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        DynamicContentMacroModuleBean dynamicMacroWithAutoconvert = newDynamicContentMacroModuleBean()
                .withUrl("/dynamic-macro")
                .withKey("dynamic-macro-with-autoconvert")
                .withName(new I18nProperty(DYNAMIC_MACRO_WITH_AUTOCONVERT, null))
                .withParameters(
                        newMacroParameterBean()
                                .withIdentifier("url")
                                .withName(new I18nProperty("URL", ""))
                                .withType("string")
                                .build())
                .withAutoconvert(AutoconvertBean.newAutoconvertBean()
                        .withUrlParameter("url")
                        .withMatchers(
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://google.com")
                                        .build(),
                                MatcherBean.newMatcherBean()
                                        .withPattern("https://google.com/{}")
                                        .build())
                        .build())
                .build();

        remotePlugin = new ConnectRunner(getProduct().getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addScope(ScopeName.ADMIN) // for using ap.request
                .addModules("dynamicContentMacros",
                        dynamicMacroWithAutoconvert
                )
                .addRoute("/dynamic-macro", ConnectAppServlets.helloWorldServlet())
                .addRoute("/dynamic-macro-static", ConnectAppServlets.dynamicMacroStaticServlet())
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
    public void testDynamicMacroWithAutoconvertOnCreate() throws Exception
    {
        EditorPage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), AbstractConfluenceWebDriverTest.TestSpace.DEMO);
        pasteLink(editorPage, "https://google.com");
    }

    @Test
    public void testDynamicMacroWithAutoconvertWithTokenOnCreate() throws Exception
    {
        EditorPage editorPage = getProduct().loginAndCreatePage(TestUser.ADMIN.confUser(), AbstractConfluenceWebDriverTest.TestSpace.DEMO);
        pasteLink(editorPage, "https://google.com/variable");
    }

    private void pasteLink(EditorPage editorPage, String link)
    {
        EditorContent editorContent = editorPage.getEditor().getContent();
        editorContent.focus();

        boolean normalizeSpaces = false;
        editorContent.setContentViaJs(editorContent.normalizeHtml(link, false));
        Poller.waitUntilTrue("editor content should contain " + link,
                editorContent.normalizedHtmlContains(editorContent.normalizeHtml(link, normalizeSpaces), normalizeSpaces));

        editorContent.placeCursorAtStart("p");

        // using select-all here doesnt appear to work, i think its because it selects the <p> tags too
        for (int i = 0; i < link.length(); i++)
        {
            editorContent.sendKeys(Keys.chord(Keys.SHIFT.toString(), Keys.ARROW_RIGHT.toString()));
        }

        editorContent.sendKeys(Keys.chord(OS_CTRL_KEY, "x")); // cut
        editorContent.sendKeys(Keys.chord(OS_CTRL_KEY, "v")); // paste

        editorPage.setTitle("TestAutoconvert-" + System.currentTimeMillis());

        ViewPage viewPage = editorPage.save();
        long pageId = viewPage.getPageId();
        String pageContent = rpc.getPageContent(pageId);
        Document doc = Jsoup.parse(pageContent, "", Parser.xmlParser());

        // check that the macro was created correctly
        Elements elements = doc.select("ac|structured-macro");
        assertEquals(1, elements.size());
        Element macroElement = elements.get(0);
        assertEquals("dynamic-macro-with-autoconvert", macroElement.attr("ac:name"));
        Elements parameterElements = macroElement.select("ac|parameter");
        assertEquals(1, parameterElements.size());
        Element urlParameter = parameterElements.get(0);
        assertEquals(link, urlParameter.text());
    }


}
