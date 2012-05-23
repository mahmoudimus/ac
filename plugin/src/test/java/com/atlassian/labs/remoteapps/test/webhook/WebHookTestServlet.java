package com.atlassian.labs.remoteapps.test.webhook;

import cc.plural.jsonij.JPath;
import cc.plural.jsonij.JSON;
import cc.plural.jsonij.Value;
import cc.plural.jsonij.parser.ParserException;
import com.atlassian.labs.remoteapps.test.RemoteAppRunner;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebHookTestServlet extends HttpServlet implements WebHookBody
{
    private volatile JSON body;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws
            ServletException,
            IOException
    {
        if (req.getRequestURI().endsWith("/webhook"))
        {
            try
            {
                body = JSON.parse(IOUtils.toString(req.getReader()));
            }
            catch (ParserException e)
            {
                throw new ServletException(e);
            }
        }
    }

    public static void runInRunner(String baseUrl, String webHookId, WebHookTester tester) throws
            Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        RemoteAppRunner runner = new RemoteAppRunner(baseUrl, webHookId)
                .addWebhook(webHookId, "/webhook", servlet)
                .start();

        tester.test(new WebHookWaiter()
        {
            @Override
            public WebHookBody waitForHook() throws Exception
            {
                servlet.assertHookFired();
                return servlet;
            }
        });

        runner.stop();
    }

    public void assertHookFired() throws InterruptedException
    {
        long expiry = System.currentTimeMillis() + 5 * 1000;

        while (expiry > System.currentTimeMillis())
        {
            if (hookFired())
            {
                break;
            }
            Thread.sleep(100);
        }
        if (!hookFired())
        {
            throw new AssertionError("Event not published");
        }
    }

    public boolean hookFired()
    {
        return body != null;
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
