package servlets;

import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
@Component
public class MyCounterMacroServlet extends HttpServlet
{
    private static final long ONE_YEAR_SECONDS = 60L * 60L * 24L * 365L;
    private static final long ONE_YEAR_MILLISECONDS = 1000 * ONE_YEAR_SECONDS;
    private int counter = 1;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        resp.setDateHeader("Expires", System.currentTimeMillis() + ONE_YEAR_MILLISECONDS);
        resp.setHeader("Cache-Control", "s-maxage=" + ONE_YEAR_SECONDS);
        PrintWriter writer = resp.getWriter();
        writer.print("<div class=\"rp-counter\">" + counter++ + "</div>");
        writer.close();
    }
}
