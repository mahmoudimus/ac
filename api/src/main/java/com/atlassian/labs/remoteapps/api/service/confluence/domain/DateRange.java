package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
public enum DateRange
{
    LASTDAY(1),
    LASTTWODAYS(2),
    LASTWEEK(7),
    LASTMONTH(31),
    LASTSIXMONTHS(365 / 2),
    LASTYEAR(365),
    LASTTWOYEARS(365 * 2);

    private final long millis;

    DateRange(long days)
    {
        millis = days * 1000 * 60 * 60 * 24;
    }
}