package it.servlet.condition;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.valueOf;

public class CheckUsernameConditionServlet extends HttpServlet
{
    private final String username;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CheckUsernameConditionServlet(String username)
    {
        this.username = username;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String loggedInUser = req.getParameter("user_id");
        final boolean isBetty = isUser(loggedInUser);

        logger.debug("The logged in user is {}betty, their user key is '{}'", isBetty ? "" : "NOT ", loggedInUser);

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

    private boolean isUser(String loggedInUser)
    {
        return username.equals(loggedInUser);
    }
}