package servlets;

import com.atlassian.labs.remoteapps.kit.servlet.Multipage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Multipage
public class MyMultipageServlet extends HttpServlet
{
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        res.setStatus(200);
        res.setHeader("Content-Type", "text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.print("Hello Multipage!");
        out.flush();
    }
}
