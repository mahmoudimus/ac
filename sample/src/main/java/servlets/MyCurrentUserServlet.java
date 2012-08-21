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

/**
 *
 */
@Singleton
public class MyCurrentUserServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;

    @Inject
    public MyCurrentUserServlet(@ServiceReference SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String consumerKey = signedRequestHandler.validateRequest(req);

        resp.setContentType("text/plain");
        resp.getWriter().write("this is the incoming url: " + req.getRequestURI());
        resp.getWriter().write("The consumer key is : " + consumerKey);
        resp.getWriter().close();
    }
}
