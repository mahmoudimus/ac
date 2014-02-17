package it.servlet;

import com.atlassian.plugin.connect.test.utils.NameValuePairs;
import org.apache.http.NameValuePair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
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
        NameValuePairs parameters = new NameValuePairs(req.getParameterMap());
        queryParameters.push(parameters);

        resp.setContentType("text/html");
        render(resp.getWriter(), parameters.getNameValuePairs());
        resp.getWriter().close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        doGet(req, resp, context);
    }

    public NameValuePairs waitForQueryParameters() throws InterruptedException
    {
        return queryParameters.poll(5, TimeUnit.SECONDS);
    }

    private void render(PrintWriter writer, List<NameValuePair> nameValuePairs)
    {
        writer.write(" <p id=\"hello-world-message\">Hello world</p>\n");

        writer.write("<ul>");
        for (NameValuePair pair : nameValuePairs)
        {
            writer.write(String.format("<li id=\"%s\">", pair.getName()));
            writer.write(pair.getName());
            writer.write(": ");
            writer.write(pair.getValue());
            writer.write("</li>\n");
        }
        writer.write("</ul>");
    }
}
