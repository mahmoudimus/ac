package com.atlassian.jira.rest.client.p3.internal;

import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.internal.json.JsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.JsonParser;
import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;
import com.google.common.util.concurrent.SettableFuture;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import static com.atlassian.labs.remoteapps.api.Promises.toPromise;

public abstract class AbstractP3RestClient
{
    protected static <T> PromiseCallback<Response> newErrorCallback(final SettableFuture<T> delegate)
    {
        return new PromiseCallback<Response>()
        {
            @Override
            public void handle(Response value)
            {
                try
                {
                    final String body = value.getEntity();
                    final Collection<String> errorMessages = extractErrors(body);
                    delegate.setException(new RestClientException(errorMessages));
                }
                catch (JSONException e)
                {
                    delegate.setException(new RestClientException(e));
                }
                catch (IOException e)
                {
                    delegate.setException(new RestClientException(e));
                }
            }
        };
    }

    protected interface ResponseHandler<T>
    {
        T handle(Response request) throws JSONException, IOException;
    }

	protected final HostHttpClient client;
	protected final URI baseUri;

	public AbstractP3RestClient(HostHttpClient client)
    {
		this.baseUri = URI.create("/rest/api/latest");
		this.client = client;
	}

    protected <T> Promise<T> callAndParse(ResponsePromise responsePromise, final ResponseHandler<T> responseHandler)
    {
        final SettableFuture<T> future = SettableFuture.create();
        PromiseCallback<Response> successCallback = new PromiseCallback<Response>()
        {
            @Override
            public void handle(Response value)
            {
                try
                {
                    T result = responseHandler.handle(value);
                    future.set(result);
                }
                catch (IOException e)
                {
                    future.setException(new RestClientException(e));
                }
                catch (JSONException e)
                {
                    future.setException(new RestClientException(e));
                }
            }
        };
        PromiseCallback<Response> notFoundCallback = new PromiseCallback<Response>()
        {
            @Override
            public void handle(Response value)
            {
                future.set(null);
            }
        };

        responsePromise
                .ok(successCallback)
                .on(404, notFoundCallback)
                .created(successCallback)
                .others(newErrorCallback(future));
        return toPromise(future);
    }

	protected <T> Promise<T> callAndParse(ResponsePromise responsePromise, final JsonParser<?, T> parser) {
        return callAndParse(responsePromise, new ResponseHandler<T>()
        {
            @Override
            public T handle(Response response) throws JSONException, IOException
            {
                String body = response.getEntity();
                return (T) (parser instanceof JsonObjectParser ?
                        ((JsonObjectParser) parser).parse(new JSONObject(body)) :
                        ((JsonArrayParser) parser).parse(new JSONArray(body)));
            }
        });
	}

	protected Promise<Void> call(ResponsePromise responsePromise) {
        final SettableFuture<Void> future = SettableFuture.create();
        responsePromise.noContent(new PromiseCallback<Response>()
        {
            @Override
            public void handle(Response value)
            {
                future.set(null);
            }
        })
                .others(newErrorCallback(future));
        return toPromise(future);
	}

	static Collection<String> extractErrors(String body) throws JSONException {
		JSONObject jsonObject = new JSONObject(body);
		final Collection<String> errorMessages = new ArrayList<String>();
		final JSONArray errorMessagesJsonArray = jsonObject.optJSONArray("errorMessages");
		if (errorMessagesJsonArray != null) {
			errorMessages.addAll(JsonParseUtil.toStringCollection(errorMessagesJsonArray));
		}
		final JSONObject errorJsonObject = jsonObject.optJSONObject("errors");
		if (errorJsonObject != null) {
			final JSONArray valuesJsonArray = errorJsonObject.toJSONArray(errorJsonObject.names());
			if (valuesJsonArray != null) {
				errorMessages.addAll(JsonParseUtil.toStringCollection(valuesJsonArray));
			}
		}
		return errorMessages;
	}

    protected <T> String toEntity(JsonGenerator<T> generator, T bean)
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


	protected static class InputGeneratorCallable<T> implements Callable<JSONObject> {

		private final JsonGenerator<T> generator;
		private final T bean;

		public static <T> InputGeneratorCallable<T> create(JsonGenerator<T> generator, T bean) {
			return new InputGeneratorCallable<T>(generator, bean);
		}

		public InputGeneratorCallable(JsonGenerator<T> generator, T bean) {
			this.generator = generator;
			this.bean = bean;
		}

		@Override
		public JSONObject call() throws Exception {
			return generator.generate(bean);
		}
	}
}
