package servlets;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@Component
public class MyCurrentUserServlet extends HttpServlet
{
    private final OAuthContext oAuthContext;

    @Autowired
    public MyCurrentUserServlet(OAuthContext oAuthContext)
    {
        this.oAuthContext = oAuthContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String consumerKey = oAuthContext.validate2LOFromParameters(req);

        resp.setContentType("text/plain");
        resp.getWriter().write("this is the incoming url: " + req.getRequestURI());
        resp.getWriter().write("The consumer key is : " + consumerKey);
        resp.getWriter().close();
    }
}
