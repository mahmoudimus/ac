package servlets;

import com.atlassian.pluginkit.servlet.AbstractPageServlet;
import com.atlassian.pluginkit.servlet.AppScripts;
import com.atlassian.pluginkit.servlet.AppStylesheets;
import com.atlassian.pluginkit.servlet.Aui;
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
        render(req, res, ImmutableMap.of(
            "message", "Hello World"
        ));
    }
}
