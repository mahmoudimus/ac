package com.atlassian.labs.remoteapps.modules.confluence;

import org.apache.poi.hwpf.model.SavedByTable;
import org.json.JSONException;
import org.json.JSONObject;

public class SavedMacroInstance
{
    private final String value;
    private final long expiry;

    public SavedMacroInstance(String jsonValue)
    {
        String value = null;
        long expiry = 0;
        try
        {
            JSONObject obj = new JSONObject(jsonValue);
            value = obj.getString("value");
            expiry = obj.getLong("expiry");
        } catch (JSONException e)
        {
            // probably an older, non-json value
            value = jsonValue;
            expiry = 0;
        }
        this.value = value;
        this.expiry = expiry;

    }

    public SavedMacroInstance(String value, long expiry)
    {
        this.value = value;
        this.expiry = expiry;
    }

    public String getValue()
    {
        return value;
    }

    public long getExpiry()
    {
        return expiry;
    }

    public String toJson()
    {
        try
        {
            return new JSONObject()
                    .put("value", value)
                    .put("expiry", expiry)
                    .toString(2);
        } catch (JSONException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }
    }
}
