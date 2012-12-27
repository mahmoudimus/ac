package servlets;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
@Named
public class I18nServlet extends HttpServlet
{
    @Inject
    @ComponentImport
    I18nResolver i18nResolver;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/plain");
        resp.getWriter().write(i18nResolver.getText(req.getParameter("message")));
        resp.getWriter().close();
    }
}
