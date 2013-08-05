package com.atlassian.plugin.remotable.test.webhook;

import cc.plural.jsonij.JPath;
import cc.plural.jsonij.JSON;
import cc.plural.jsonij.Value;
import cc.plural.jsonij.parser.ParserException;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.module.WebhookModule;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public final class WebHookTestServlet extends HttpServlet
{
    private volatile BlockingDeque<WebHookBody> webHooksQueue = new LinkedBlockingDeque<WebHookBody>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (req.getRequestURI().endsWith("/webhook"))
        {
            try
            {
                webHooksQueue.push(new JsonWebHookBody(JSON.parse(IOUtils.toString(req.getReader()))));
            }
            catch (ParserException e)
            {
                throw new ServletException(e);
            }
        }
    }

    public static void runInRunner(String baseUrl, String webHookId, WebHookTester tester) throws Exception
    {
        runInRunner(baseUrl, webHookId, webHookId, tester);
    }

    public static void runInRunner(String baseUrl, String webHookId, String eventId, WebHookTester tester) throws Exception
    {
        final String path = "/webhook";
        final WebHookTestServlet servlet = new WebHookTestServlet();
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(baseUrl, webHookId)
                .add(WebhookModule.key(webHookId + path.hashCode())
                        .path(path)
                        .event(eventId)
                        .resource(servlet))
                .start();

        tester.test(new WebHookWaiter()
        {
            @Override
            public WebHookBody waitForHook() throws Exception
            {
                return servlet.waitForHook();
            }
        });

        runner.stop();
    }

    public WebHookBody waitForHook() throws InterruptedException
    {
        return webHooksQueue.poll(5, TimeUnit.SECONDS);
    }

    private static final class JsonWebHookBody implements WebHookBody
    {
        private volatile JSON body;

        private JsonWebHookBody(JSON body)
        {
            this.body = body;
        }

        @Override
        public String find(String expression) throws Exception
        {
            JPath path = JPath.parse(expression);
            Value value = path.evaluate(body);
            if (value == null)
            {
                System.out.println("Can't find expression '" + expression + "' in\n" + body.toJSON());
                return null;
            }
            else
            {
                return value.toString();
            }
        }
    }
}
