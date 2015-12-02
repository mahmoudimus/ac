package it.confluence.servlet;

import javax.servlet.http.HttpServlet;

import com.atlassian.connect.test.confluence.pageobjects.RemoteMacroEditorDialog;
import com.atlassian.plugin.connect.test.common.servlet.HttpContextServlet;
import com.atlassian.plugin.connect.test.common.servlet.MustacheServlet;
import com.atlassian.plugin.connect.test.common.servlet.TestServletContextExtractor;

import static com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets.wrapContextAwareServlet;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Utility methods for creating test servlets suitable for serving Confluence-specific Connect iframes.
 */
public class ConfluenceAppServlets
{
    public static HttpServlet dynamicMacroStaticServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("it/confluence/macro/dynamic-macro-static.mu"));
    }

    public static HttpServlet macroEditor()
    {
        return wrapContextAwareServlet(new MustacheServlet(RemoteMacroEditorDialog.TEMPLATE_PATH));
    }

    public static HttpServlet macroBodyEditor(String newMacroBody)
    {
        HttpContextServlet contextServlet = new HttpContextServlet(new MustacheServlet("it/confluence/macro/editor-macro-body.mu"));
        contextServlet.getBaseContext().put("newMacroBody", newMacroBody);
        return contextServlet;
    }

    public static HttpServlet blueprintTemplateServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("it/confluence/blueprint/blueprint.mu"));
    }

    public static HttpServlet blueprintContextServlet()
    {
        return wrapContextAwareServlet(
                new MustacheServlet("it/confluence/blueprint/context.json", true),
                newArrayList(
                        new TestServletContextExtractor("parentPageId"),
                        new TestServletContextExtractor("addonKey"),
                        new TestServletContextExtractor("spaceKey"),
                        new TestServletContextExtractor("userKey"),
                        new TestServletContextExtractor("blueprintKey")
                )
        );
    }

}
