package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.atlassian.plugin.remotable.api.service.http.bigpipe.Channel;
import com.atlassian.util.concurrent.Promise;
import com.google.common.util.concurrent.FutureCallback;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractChannel implements Channel
{
    private final String id;

    private int retainCount;

    protected AbstractChannel(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public final void retainWhile(Promise promise)
    {
        retain();
        registerHandlers(promise);
    }

    private void registerHandlers(Promise promise)
    {
        checkNotNull(promise);
        promise.then(new FutureCallback()
        {
            @Override
            public void onSuccess(Object result)
            {
                handleSuccess(result);
            }

            @Override
            public void onFailure(Throwable t)
            {
                release();
            }
        });

    }

    private <A> void handleSuccess(A result)
    {
        if (result instanceof Promise)
        {
            registerHandlers((Promise) result);
        }
        else
        {
            release();
        }
    }

    Channel retain()
    {
        retainCount++;
        return this;
    }

    boolean isRetained()
    {
        return retainCount > 0;
    }

    Channel release()
    {
        retainCount--;
        return this;
    }
}
