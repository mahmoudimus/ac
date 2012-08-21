package servlets;

import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
@Singleton
public class MyImageMacroServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;

    @Inject
    public MyImageMacroServlet(@ServiceReference SignedRequestHandler signedRequestHandler)
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
