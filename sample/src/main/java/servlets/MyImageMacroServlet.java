package servlets;

import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MyImageMacroServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;

    @Autowired
    public MyImageMacroServlet(SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.print("<img src=\"" + signedRequestHandler.getLocalBaseUrl() + "/sandcastles.jpg\" alt=\"sandcastles\"/>");
        writer.close(); 
    }
}
