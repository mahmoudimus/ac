package servlets;

import com.atlassian.plugin.remotable.api.service.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.valueOf;

@Named
public class OnlyBettyConditionServlet extends HttpServlet
{
    private static final String BETTY = "betty";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestContext requestContext;

    @Inject
    public OnlyBettyConditionServlet(RequestContext requestContext)
    {
        this.requestContext = requestContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String loggedInUser = requestContext.getUserId();
        final boolean isBetty = isBetty(loggedInUser);

        logger.debug("The logged in user is {}betty, here is their username '{}'", isBetty ? "" : "NOT ", loggedInUser);

        final String json = getJson(isBetty);
        logger.debug("Responding with the following json: {}", json);
        sendJson(resp, json);
    }

    private void sendJson(HttpServletResponse resp, String json) throws IOException
    {
        resp.setContentType("application/json");
        resp.getWriter().write(json);
        resp.getWriter().close();
    }

    private String getJson(boolean shouldDisplay)
    {
        return "{\"shouldDisplay\" : " + valueOf(shouldDisplay) + "}";
    }

    private boolean isBetty(String loggedInUser)
    {
        return BETTY.equals(loggedInUser);
    }
}
