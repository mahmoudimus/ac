package com.atlassian.plugin.connect.api.util.http;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ContentRetrievalErrors
{
    private final List<String> messages;
    private final Map<String, String> fieldErrors;

    public ContentRetrievalErrors(List<String> message)
    {
        this(message, ImmutableMap.<String, String>of());
    }

    public ContentRetrievalErrors(List<String> messages, Map<String, String> fieldErrors)
    {
        this.messages = ImmutableList.copyOf(checkNotNull(messages));
        this.fieldErrors = ImmutableMap.copyOf(checkNotNull(fieldErrors));
    }

    public List<String> getMessages()
    {
        return messages;
    }

    public Map<String, String> getFieldErrors()
    {
        return fieldErrors;
    }

    public boolean hasErrors()
    {
        return !messages.isEmpty() || !fieldErrors.isEmpty();
    }

    public static ContentRetrievalErrors fromJson(String errorsAsJson)
    {
        try
        {
            final JSONObject errors = new JSONObject(errorsAsJson);
            final ImmutableList.Builder<String> messagesBuilder = ImmutableList.builder();
            if (errors.has("messages"))
            {
                final JSONArray messages = errors.getJSONArray("messages");
                for (int i = 0; i < messages.length(); i++)
                {
                    messagesBuilder.add(messages.getString(i));
                }
            }

            final ImmutableMap.Builder<String, String> fieldErrorsBuilder = ImmutableMap.builder();
            if (errors.has("fields"))
            {
                final JSONArray fieldErrors = errors.getJSONArray("fields");
                for (int i = 0; i < fieldErrors.length(); i++)
                {
                    final JSONObject fieldError = fieldErrors.getJSONObject(i);
                    fieldErrorsBuilder.put(fieldError.getString("name"), fieldError.getString("message"));
                }
            }

            return new ContentRetrievalErrors(messagesBuilder.build(), fieldErrorsBuilder.build());
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

}
