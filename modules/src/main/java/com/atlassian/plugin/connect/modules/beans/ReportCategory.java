package com.atlassian.plugin.connect.modules.beans;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.Map;

public enum ReportCategory
{
    AGILE("agile"),
    ISSUE_ANALYSIS("issue.analysis"),
    FORECAST_MANAGEMENT("forecast.management"),
    OTHER("other");

    private static final Map<String, ReportCategory> BY_KEY;

    private final String key;

    private ReportCategory(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public static Optional<ReportCategory> byKey(String key)
    {
        return Optional.fromNullable(BY_KEY.get(key));
    }

    static
    {
        ImmutableList<ReportCategory> categories = ImmutableList.of(AGILE, ISSUE_ANALYSIS, FORECAST_MANAGEMENT, OTHER);
        BY_KEY = Maps.uniqueIndex(categories, new Function<ReportCategory, String>()
        {
            @Override
            public String apply(final ReportCategory input)
            {
                return input.getKey();
            }
        });
    }
}
