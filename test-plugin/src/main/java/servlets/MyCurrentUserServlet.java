package servlets;

import com.atlassian.plugin.remotable.api.annotation.ServiceReference;
import com.atlassian.plugin.remotable.api.service.RequestContext;

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
    private final RequestContext requestContext;

    @Inject
    public MyCurrentUserServlet(@ServiceReference RequestContext requestContext)
    {
        this.requestContext = requestContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/plain");
        resp.getWriter().write("this is the incoming url: " + req.getRequestURI());
        resp.getWriter().write("The consumer key is : " + requestContext.getClientKey());
        resp.getWriter().close();
    }
}
