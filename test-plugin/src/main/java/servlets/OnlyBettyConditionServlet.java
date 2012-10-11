package servlets;

import com.atlassian.plugin.remotable.api.service.RequestContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
@Singleton
public class OnlyBettyConditionServlet extends HttpServlet
{
    private final RequestContext requestContext;

    @Inject
    public OnlyBettyConditionServlet(RequestContext requestContext)
    {
        this.requestContext = requestContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        String value = "betty".equals(requestContext.getUserId()) ? "true" : "false";
        resp.setContentType("application/json");
        resp.getWriter().write("{\"shouldDisplay\" : " + value + "}");
        resp.getWriter().close();
    }
}
