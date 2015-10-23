package com.atlassian.plugin.connect.plugin.rest.data;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A representation of a contact person for a product host, for use in the REST API.
 */
public class RestContact
{

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String email;

    public RestContact(String name, String email)
    {
        this.name = name;
        this.email = email;
    }

    public String getName()
    {
        return name;
    }

    public String getEmail()
    {
        return email;
    }
}
