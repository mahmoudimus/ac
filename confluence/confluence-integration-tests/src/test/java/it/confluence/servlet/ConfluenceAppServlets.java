package it.confluence.servlet;

import javax.servlet.http.HttpServlet;

import com.atlassian.connect.test.jira.pageobjects.RemoteMacroEditorDialog;

import it.servlet.HttpContextServlet;
import it.servlet.iframe.MustacheServlet;

import static it.servlet.ConnectAppServlets.wrapContextAwareServlet;

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
}
