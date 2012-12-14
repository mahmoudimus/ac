package servlets;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@Named
public class MySlowMacroServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        int sleepSeconds = req.getParameter("sleep") != null ? Integer.parseInt(req.getParameter("sleep")) : 22;
        try
        {
            Thread.sleep(sleepSeconds * 1000);
        }
        catch (InterruptedException e)
        {
            // do nothing
        }
        resp.setContentType("text/html");
        resp.getWriter().write("finished");
        resp.getWriter().close();
    }
}
