package com.atlassian.connect.capabilities.client;

import java.lang.reflect.Type;

import com.google.gson.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @since version
 */
public class DateTimeTypeAdapter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime>
{

    @Override
    public DateTime deserialize(final JsonElement je, final Type type, final JsonDeserializationContext jdc) throws JsonParseException
    {
        return je.getAsString().length() == 0 ? UniversalDateFormatter.NULL_DATE : UniversalDateFormatter.parse(je.getAsString());
    }

    @Override
    public JsonElement serialize(final DateTime src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        return new JsonPrimitive(src == null ? UniversalDateFormatter.format(UniversalDateFormatter.NULL_DATE) : UniversalDateFormatter.format(src));
    }

}
