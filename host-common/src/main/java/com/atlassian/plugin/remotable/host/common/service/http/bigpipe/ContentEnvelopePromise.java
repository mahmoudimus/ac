package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.atlassian.util.concurrent.ForwardingPromise;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.Collections.emptyMap;

class ContentEnvelopePromise extends ForwardingPromise<JSONObject> implements Promise<JSONObject>
{
    private final Promise<JSONObject> delegate;

    private final Map<String, String> metadata;

    ContentEnvelopePromise(Promise<String> delegate, final String channelId)
    {
        if (delegate instanceof MetadataProvider)
        {
            this.metadata = ((MetadataProvider) delegate).getMetadata();
        }
        else
        {
            this.metadata = emptyMap();
        }

        this.delegate = delegate.map(new Function<String, JSONObject>()
        {
            @Override
            public JSONObject apply(@Nullable String content)
            {
                JSONObject obj = new JSONObject();
                obj.put("channelId", channelId);
                obj.put("content", content);
                obj.putAll(metadata);
                return obj;
            }
        });
    }

    @Override
    protected Promise<JSONObject> delegate()
    {
        return delegate;
    }
}
