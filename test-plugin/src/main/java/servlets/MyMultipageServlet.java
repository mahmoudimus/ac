package servlets;

import com.atlassian.plugin.remotable.kit.servlet.*;
import com.google.common.collect.ImmutableMap;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

@Singleton
@Multipage
public class MyMultipageServlet extends AbstractPageServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        render(req, res, ImmutableMap.of("name", "World"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        String name = req.getParameter("name");
        name = name != null && !name.trim().isEmpty() ? name.trim() : "World";
        render(req, res, ImmutableMap.of("name", name));
    }
}
