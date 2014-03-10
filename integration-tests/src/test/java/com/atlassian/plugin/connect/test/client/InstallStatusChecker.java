package com.atlassian.plugin.connect.test.client;

import java.util.concurrent.*;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import cc.plural.jsonij.JSON;

/**
 * Checks the add-on installation status in regular intervals (avoids busy polling)
 */
public class InstallStatusChecker
{
    private final String statusUrl;
    private final long timeout;
    private final long period;
    private final ScheduledExecutorService scheduledExecutor;
    private final UserRequestSender userRequestSender;

    public InstallStatusChecker(UserRequestSender userRequestSender, String statusUrl, long timeout, TimeUnit timeoutUnit, long period, TimeUnit periodUnit)
    {
        this.userRequestSender = userRequestSender;
        this.statusUrl = statusUrl;
        this.timeout = timeoutUnit.toMillis(timeout);
        this.period = periodUnit.toMillis(period);
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void run(final String defaultUsername, final String defaultPassword) throws Exception
    {
        Callable<Boolean> statusChecker = new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                HttpGet statusGet = new HttpGet(statusUrl);
                ResponseHandler<String> statusHandler = new BasicResponseHandler();
                String response = userRequestSender.sendRequestAsUser(statusGet, statusHandler, defaultUsername, defaultPassword);
                if (null != response && StringUtils.isNotBlank(response) && response.startsWith("{") && response.endsWith("}"))
                {
                    JSON json = JSON.parse(response);
                    return (null != json.get("enabled"));
                }
                return false;
            }
        };

        ScheduledFuture<Boolean> statusCheck = scheduledExecutor.schedule(statusChecker, period, TimeUnit.MILLISECONDS);

        long abortAfter = System.currentTimeMillis() + timeout;

        while (!statusCheck.get() && abortAfter > System.currentTimeMillis())
        {
            statusCheck = scheduledExecutor.schedule(statusChecker, period, TimeUnit.MILLISECONDS);
        }

        if (abortAfter <= System.currentTimeMillis())
        {
            throw new Exception("Connect App Plugin did not install within the allotted timeout");
        }
    }
}
