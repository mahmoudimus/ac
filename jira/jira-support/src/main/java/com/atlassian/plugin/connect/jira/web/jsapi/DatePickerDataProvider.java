package com.atlassian.plugin.connect.jira.web.jsapi;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.json.marshal.Jsonable;
import com.atlassian.webresource.api.data.WebResourceDataProvider;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;

public class DatePickerDataProvider implements WebResourceDataProvider {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ApplicationProperties applicationProperties;

    public DatePickerDataProvider(JiraAuthenticationContext jiraAuthenticationContext, ApplicationProperties applicationProperties) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Jsonable get() {
        return (writer) -> {
            try {
                getJsonData().write(writer);
            } catch (JSONException e) {
                throw new Jsonable.JsonMappingException(e);
            }
        };
    }

    private JSONObject getJsonData() {
        ImmutableMap.Builder<String, Object> values = ImmutableMap.builder();
        Calendar calendar = Calendar.getInstance(jiraAuthenticationContext.getLocale());

        values.put("dateFormat", DateTimeFormatUtils.getDateFormat());
        values.put("dateTimeFormat", DateTimeFormatUtils.getDateTimeFormat());
        values.put("timeFormat", DateTimeFormatUtils.getTimeFormat());

        values.put("firstDay", calendar.getFirstDayOfWeek());
        values.put("useISO8601WeekNumbers", applicationProperties.getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601));


        return new JSONObject(values.build());
    }
}
