package com.atlassian.connect.capabilities.client;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @since version
 */
public class UniversalDateFormatter
{
    private static final DateTimeFormatter FORMATTER = ISODateTimeFormat.dateTimeNoMillis();

    public static final DateTime NULL_DATE = new DateTime(0).secondOfDay().roundFloorCopy();

    private UniversalDateFormatter()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static DateTime parse(String date)
    {
        return FORMATTER.parseDateTime(date);
    }

    public static String format(DateTime date)
    {
        return FORMATTER.withZone(DateTimeZone.UTC).print(date);
    }
}
