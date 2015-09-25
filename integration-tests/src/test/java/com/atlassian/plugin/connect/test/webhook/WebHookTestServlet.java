package com.atlassian.plugin.connect.test.webhook;

import cc.plural.jsonij.JPath;
import cc.plural.jsonij.JSON;
import cc.plural.jsonij.Value;
import cc.plural.jsonij.parser.ParserException;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.HttpHeaderNames;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static com.atlassian.plugin.connect.modules.beans.WebHookModuleBean.newWebHookBean;
import static com.atlassian.plugin.connect.test.AddonTestUtils.randomWebItemBean;

public final class WebHookTestServlet extends HttpServlet
{

    private static final Logger log = LoggerFactory.getLogger(WebHookTestServlet.class);

    private volatile BlockingDeque<WebHookBody> webHooksQueue = new LinkedBlockingDeque<WebHookBody>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (req.getRequestURI().endsWith("/webhook") || req.getRequestURI().endsWith("-lifecycle"))
        {
            String version = req.getHeader(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION);
            try
            {
                webHooksQueue.push(new JsonWebHookBody(getFullURL(req), JSON.parse(IOUtils.toString(req.getReader())), version));
                resp.getWriter().write("OKEY DOKEY");
            } catch (ParserException e)
            {
                throw new ServletException(e);
            }

        }
    }

    public static void runInRunner(String baseUrl, String eventId, String pluginKey, WebHookTester tester) throws Exception
    {
        final String webHookPath = "/webhook";
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner runner = new ConnectRunner(baseUrl, pluginKey)
                .addModule("webhooks", newWebHookBean()
                        .withUrl(webHookPath)
                        .withEvent(eventId)
                        .build())
                .addRoute(webHookPath, servlet)
                .addScope(ScopeName.READ)
                .addJWT(new WebHookTestServlet()) // different servlet for installed callback so that tests can inspect only the webhooks
                .start();

        try
        {
            tester.test(new WebHookWaiter()
            {
                @Override
                public WebHookBody waitForHook() throws Exception
                {
                    return servlet.waitForHook();
                }
            });
        }
        finally
        {
            runner.stopAndUninstall();
        }
    }

    public static void runInJsonRunner(String baseUrl, String webHookId, WebHookTester tester) throws Exception
    {
        runInJsonRunner(baseUrl, webHookId, webHookId, tester);
    }

    public static void runInstallInJsonRunner(String baseUrl, String pluginKey, WebHookTester tester) throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner runner = new ConnectRunner(baseUrl, pluginKey)
                .addInstallLifecycle()
                .addModule("webItems", randomWebItemBean())
                .addRoute(ConnectRunner.INSTALLED_PATH, servlet)
                .start();

        try
        {
            tester.test(new WebHookWaiter()
            {
                @Override
                public WebHookBody waitForHook() throws Exception
                {
                    return servlet.waitForHook();
                }
            });
        }
        finally
        {
            runner.stopAndUninstall();
        }
    }

    public static void runEnableInJsonRunner(String baseUrl, String pluginKey, WebHookTester tester) throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner runner = new ConnectRunner(baseUrl, pluginKey)
                .setAuthenticationToNone()
                .addEnableLifecycle()
                .addModule("webItems", randomWebItemBean())
                .addRoute(ConnectRunner.ENABLED_PATH, servlet)
                .start();

        try
        {
            tester.test(new WebHookWaiter()
            {
                @Override
                public WebHookBody waitForHook() throws Exception
                {
                    return servlet.waitForHook();
                }
            });
        }
        finally
        {
            runner.stopAndUninstall();
        }
    }

    public static void runDisableInJsonRunner(String baseUrl, String pluginKey, WebHookTester tester) throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner runner = new ConnectRunner(baseUrl, pluginKey)
                .addDisableLifecycle()
                .addModule("webItems",randomWebItemBean())
                .addRoute(ConnectRunner.DISABLED_PATH, servlet)
                .start();

        try
        {
            tester.test(new WebHookWaiter()
            {
                @Override
                public WebHookBody waitForHook() throws Exception
                {
                    return servlet.waitForHook();
                }
            });
        }
        finally
        {
            runner.stopAndUninstall();
        }
    }

    public static void runUninstalledInJsonRunner(String baseUrl, String pluginKey, WebHookTester tester) throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner runner = new ConnectRunner(baseUrl, pluginKey)
                .addUninstallLifecycle()
                .addModule("webItems", randomWebItemBean())
                .addRoute(ConnectRunner.UNINSTALLED_PATH, servlet)
                .start();

        try
        {
            tester.test(new WebHookWaiter()
            {
                @Override
                public WebHookBody waitForHook() throws Exception
                {
                    return servlet.waitForHook();
                }
            });
        }
        finally
        {
            runner.stopAndUninstall();
        }
    }

    public static void runInJsonRunner(String baseUrl, String addOnKey, String eventId, WebHookTester tester) throws Exception
    {
        final String path = "/webhook";
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner runner = new ConnectRunner(baseUrl, addOnKey)
                .setAuthenticationToNone()
                .addModule("webhooks", newWebHookBean().withEvent(eventId).withUrl(path).build())
                .addRoute(path, servlet)
                .addModule("webItems", randomWebItemBean())
                .addScope(ScopeName.READ) // for receiving web hooks
                .start();

        try
        {
            tester.test(new WebHookWaiter()
            {
                @Override
                public WebHookBody waitForHook() throws Exception
                {
                    return servlet.waitForHook();
                }
            });
        }
        finally
        {
            runner.stopAndUninstall();
        }
    }

    public static String getFullURL(HttpServletRequest request)
    {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null)
        {
            return requestURL.toString();
        } else
        {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    public WebHookBody waitForHook() throws InterruptedException
    {
        return webHooksQueue.poll(10, TimeUnit.SECONDS);
    }

    private static final class JsonWebHookBody implements WebHookBody
    {
        private volatile JSON body;
        private volatile String requestURI;
        private volatile String version;

        private JsonWebHookBody(String requestURI, JSON body, String version)
        {
            this.requestURI = requestURI;
            this.body = body;
            this.version = version;
        }

        @Override
        public String find(String expression) throws Exception
        {
            JPath path = JPath.parse(expression);
            Value value = path.evaluate(body);
            if (value == null)
            {
                log.warn("Can't find expression '" + expression + "' in\n" + body.toJSON());
                return null;
            } else
            {
                return value.toString();
            }
        }

        @Override
        public URI getRequestURI() throws Exception
        {
            return new URI(requestURI);
        }

        @Override
        public String getConnectVersion()
        {
            return this.version;
        }
    }
}
