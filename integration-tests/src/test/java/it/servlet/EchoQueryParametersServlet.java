package it.servlet;

import com.atlassian.plugin.connect.test.utils.NameValuePairs;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class EchoQueryParametersServlet extends ContextServlet
{
    private volatile BlockingDeque<NameValuePairs> queryParameters = new LinkedBlockingDeque<NameValuePairs>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        String queryString = req.getQueryString();
        queryParameters.push(new NameValuePairs(queryString));

        resp.setContentType("text/plain");
        resp.getWriter().write(queryString);
        resp.getWriter().close();
    }

    public NameValuePairs waitForQueryParameters() throws InterruptedException
    {
        return queryParameters.poll(5, TimeUnit.SECONDS);
    }
}
