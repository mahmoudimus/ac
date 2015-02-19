package com.atlassian.plugin.connect.plugin.rest.data;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * A representation of an add-on license for the REST API.
 */
public class RestHost
{

    @JsonProperty
    private final String product;

    @JsonProperty
    private final List<RestContact> contacts;

    public RestHost(String product, List<RestContact> contacts)
    {
        this.product = product;
        this.contacts = contacts;
    }

    public String getProduct()
    {
        return product;
    }

    public List<RestContact> getContacts()
    {
        return contacts;
    }
}
