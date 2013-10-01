package com.atlassian.connect.capabilities.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

/**
 * @since version
 */
public class CapabilitiesGsonFactory
{
    public static GsonBuilder getGsonBuilder()
    {
        return new GsonBuilder().registerTypeAdapter(DateTime.class,new DateTimeTypeAdapter());
    }

    public static Gson getGson()
    {
        return getGsonBuilder().create();
    }
}
