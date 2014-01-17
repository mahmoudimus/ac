package it.servlet;

import com.atlassian.plugin.connect.test.HttpUtils;
import com.google.common.collect.Maps;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class EchoContextServlet extends ContextServlet
{
    private volatile BlockingDeque<Map<String, Object>> contexts = new LinkedBlockingDeque<Map<String, Object>>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        contexts.push(Maps.newHashMap(context));
        HttpUtils.renderHtml(resp, "http-context.mu", context);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        doGet(req, resp, context);
    }

    public Map<String, Object> waitForContext() throws InterruptedException
    {
        return contexts.poll(5, TimeUnit.SECONDS);
    }
}
