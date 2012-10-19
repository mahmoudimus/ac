package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.internal.json.JsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.JsonParser;
import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.*;

public abstract class AbstractP3RestClient
{
    protected interface ResponseHandler<T>
    {
        T handle(Response request) throws JSONException, IOException;
    }

    protected final HostHttpClient client;
    protected final URI baseUri;

    protected AbstractP3RestClient(HostHttpClient client)
    {
        this.baseUri = URI.create("/rest/api/latest");
        this.client = checkNotNull(client);
    }

    protected final <T> Promise<T> callAndParse(ResponsePromise responsePromise, final ResponseHandler<T> responseHandler)
    {
        final Function<Response, ? extends T> transformFunction = toFunction(responseHandler);

        return responsePromise.<T>transform()
                .ok(transformFunction)
                .created(transformFunction)
                .notFound(constant((T) null))
                .others(AbstractP3RestClient.<T>errorFunction());
    }

    protected final <T> Promise<T> callAndParse(ResponsePromise responsePromise, final JsonParser<?, T> parser)
    {
        return callAndParse(responsePromise, new ResponseHandler<T>()
        {
            @Override
            public T handle(Response response) throws JSONException, IOException
            {
                final String body = response.getEntity();

                @SuppressWarnings("unchecked")
                final T parsed = (T) (parser instanceof JsonObjectParser ?
                        ((JsonObjectParser) parser).parse(new JSONObject(body)) :
                        ((JsonArrayParser) parser).parse(new JSONArray(body)));
                return parsed;
            }
        });
    }

    protected final Promise<Void> call(ResponsePromise responsePromise)
    {
        return responsePromise.<Void>transform()
                .noContent(constant((Void) null))
                .others(AbstractP3RestClient.<Void>errorFunction());
    }

    private static <T> Function<Response, ? extends T> toFunction(final ResponseHandler<T> responseHandler)
    {
        return new Function<Response, T>()
        {
            @Override
            public T apply(@Nullable Response input)
            {
                try
                {
                    return responseHandler.handle(input);
                }
                catch (JSONException e)
                {
                    throw new RestClientException(e);
                }
                catch (IOException e)
                {
                    throw new RestClientException(e);
                }
            }
        };
    }

    private static <T> Function<Response, T> errorFunction()
    {
        return new Function<Response, T>()
        {
            @Override
            public T apply(Response response)
            {
                try
                {
                    final String body = response.getEntity();
                    final Collection<String> errorMessages = extractErrors(body);
                    throw new RestClientException(errorMessages);
                }
                catch (JSONException e)
                {
                    throw new RestClientException(e);
                }
            }
        };
    }

    private static <T> Function<Response, T> constant(final T value)
    {
        return new Function<Response, T>()
        {
            @Override
            public T apply(Response input)
            {
                return value;
            }
        };
    }

    private static Collection<String> extractErrors(String body) throws JSONException
    {
        if (body == null)
        {
            return Collections.emptyList();
        }
        JSONObject jsonObject = new JSONObject(body);
        final Collection<String> errorMessages = new ArrayList<String>();
        final JSONArray errorMessagesJsonArray = jsonObject.optJSONArray("errorMessages");
        if (errorMessagesJsonArray != null)
        {
            errorMessages.addAll(JsonParseUtil.toStringCollection(errorMessagesJsonArray));
        }
        final JSONObject errorJsonObject = jsonObject.optJSONObject("errors");
        if (errorJsonObject != null)
        {
            final JSONArray valuesJsonArray = errorJsonObject.toJSONArray(errorJsonObject.names());
            if (valuesJsonArray != null)
            {
                errorMessages.addAll(JsonParseUtil.toStringCollection(valuesJsonArray));
            }
        }
        return errorMessages;
    }

    protected final <T> String toEntity(JsonGenerator<T> generator, T bean)
    {
        try
        {
            return generator.generate(bean).toString();
        }
        catch (JSONException e)
        {
            throw new RestClientException(e);
        }
    }
}
