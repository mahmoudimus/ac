package it.servlet.condition;

import it.util.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.valueOf;

public class CheckUsernameConditionServlet extends HttpServlet
{
    private final String username;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CheckUsernameConditionServlet(TestUser user)
    {
        this.username = user.getUsername();
    }

    public CheckUsernameConditionServlet(String username)
    {
        this.username = username;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String loggedInUser = req.getParameter("user_id");

        final String json = getJson(isUser(loggedInUser));
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

    private boolean isUser(String loggedInUser)
    {
        return username.equals(loggedInUser);
    }
}