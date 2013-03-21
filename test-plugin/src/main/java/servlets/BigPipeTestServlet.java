package servlets;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipeManager;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.ConsumableBigPipe;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.DataChannel;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.HtmlChannel;
import com.atlassian.pluginkit.servlet.AbstractPageServlet;
import com.atlassian.pluginkit.servlet.AppScripts;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.base.Function;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.atlassian.util.concurrent.Promises.promise;

@Named
@AppScripts({ "jquery-1.7", "big-pipe-test" })
public class BigPipeTestServlet extends AbstractPageServlet
{
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Inject
    @ComponentImport
    BigPipeManager bigPipeManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        if (req.getParameter("xhr") == null)
        {
            DataChannel data = bigPipeManager.getBigPipe().getDataChannel("data");
            data.promiseContent(delayedPromiseFor("{\"id\":\"data-1\",\"data\":\"my bigpipe data 1\"}", 500));
            data.promiseContent(delayedPromiseFor("{\"id\":\"data-2\",\"data\":\"my bigpipe data 2\"}", 1000));

            final HtmlChannel html = bigPipeManager.getBigPipe().getHtmlChannel();
            html.promiseContent(promise("my bigpipe <b>html</b> 2"));
            render(req, res, new HashMap<String, String>()
            {{
                put("htmlOneHtml", html.promiseContent(delayedPromiseFor("my bigpipe html 1", 500)));
                put("htmlTwoHtml", html.promiseContent(delayedPromiseFor("my bigpipe html 2", 1000)));
            }});
        }
        else
        {
            DataChannel data = bigPipeManager.getBigPipe().getDataChannel("data");
            data.promiseContent(delayedPromiseFor("{\"id\":\"xhr-data-1\",\"data\":\"my bigpipe xhr data 1\"}", 500));
            data.promiseContent(delayedPromiseFor("{\"id\":\"xhr-data-2\",\"data\":\"my bigpipe xhr data 2\"}", 1000));

            String requestId = bigPipeManager.getConsumableBigPipe().map(new Function<ConsumableBigPipe, String>()
            {
                @Override
                public String apply(ConsumableBigPipe bigPipe)
                {
                    return bigPipe.getRequestId();
                }
            }).getOrNull();

            res.setStatus(HttpServletResponse.SC_OK);
            res.setHeader("Content-Type", "application/json");
            res.setHeader("Cache-Control", "no-cache");
            res.getWriter().write("{\"requestId\": \"" + requestId + "\"}");
        }
    }

    private Promise<String> delayedPromiseFor(final String content, long millis)
    {
        return Promises.forFuture(scheduler.schedule(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return content;
            }
        }, millis, TimeUnit.MILLISECONDS));
    }
}
