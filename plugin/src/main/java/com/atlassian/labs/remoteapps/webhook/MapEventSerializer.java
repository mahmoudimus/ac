package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.labs.remoteapps.webhook.external.EventSerializer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 *
 */
public class MapEventSerializer implements EventSerializer
{
    private final Object event;
    private final Map<String, Object> data;

    public MapEventSerializer(Object event, Map<String,Object> data)
    {
        this.event = event;
        this.data = data;
    }


    @Override
    public Object getEvent()
    {
        return event;
    }

    @Override
    public String getJson()
    {
        try
        {
            return new JSONObject(data).toString(2);
        }
        catch (JSONException e)
        {
            throw new EventSerializationException(e);
        }
    }
}
