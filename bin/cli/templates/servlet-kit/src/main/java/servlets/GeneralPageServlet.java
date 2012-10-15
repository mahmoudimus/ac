package servlets;

import com.atlassian.plugin.remotable.kit.servlet.AbstractPageServlet;
import com.atlassian.plugin.remotable.kit.servlet.AppScripts;
import com.atlassian.plugin.remotable.kit.servlet.AppStylesheets;
import com.atlassian.plugin.remotable.kit.servlet.Aui;
import com.google.common.collect.ImmutableMap;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
@AppStylesheets({"styles"})
@AppScripts({"client"})
public class GeneralPageServlet extends AbstractPageServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        render(req, res, ImmutableMap.<String, Object>of(
            "message", req.getParameter("Hello World")
        ));
    }
}
