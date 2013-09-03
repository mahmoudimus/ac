package com.atlassian.connect.capabilities.client;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since version
 */
public class EntityEatingResponseHandler<T> implements ResponseHandler<T>
{
    private final ResponseHandler<T> delegatee;

    public EntityEatingResponseHandler(@Nonnull final ResponseHandler<T> delegatee)
    {
        this.delegatee = checkNotNull(delegatee);
    }

    @Override
    public T handleResponse(final HttpResponse response) throws ClientProtocolException, IOException
    {
        try
        {
            return delegatee.handleResponse(response);
        }
        finally
        {
            cleanUp(response);
        }
    }

    private void cleanUp(@Nullable final HttpResponse response) throws IOException
    {
        if (response != null)
        {
            EntityUtils.consume(response.getEntity());
        }
    }
}
