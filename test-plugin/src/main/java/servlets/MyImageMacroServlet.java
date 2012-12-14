package servlets;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
@Named
public class MyImageMacroServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;

    @Inject
    public MyImageMacroServlet(@ComponentImport SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.print("<img src=\"" + signedRequestHandler.getLocalBaseUrl() + "/public/sandcastles.jpg\" alt=\"sandcastles\"/>");
        writer.close(); 
    }
}
