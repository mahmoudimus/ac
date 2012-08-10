package servlets;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static services.HttpUtils.renderHtml;

/**
 *
 */
@Singleton
public class InfoServlet extends HttpServlet
{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String appKey = "app1";
        renderHtml(resp, "info-page.mu", Collections.<String, Object>singletonMap("appKey", appKey));

    }
}
