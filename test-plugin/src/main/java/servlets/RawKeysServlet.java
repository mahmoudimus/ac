package servlets;

import com.atlassian.plugin.remotable.kit.servlet.AppUrl;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Named
@AppUrl("/search/keys")
public class RawKeysServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/plain");
        PrintWriter writer = resp.getWriter();
        writer.print(req.getParameter("issues"));
        writer.close();
    }
}
