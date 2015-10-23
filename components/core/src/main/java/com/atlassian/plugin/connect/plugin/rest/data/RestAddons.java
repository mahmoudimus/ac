package com.atlassian.plugin.connect.plugin.rest.data;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class RestAddons<T extends RestMinimalAddon>
{
    @JsonProperty
    private final List<T> addons;

    public RestAddons(@JsonProperty ("addons") List<T> addons)
    {
        this.addons = addons;
    }

    public List<T> getAddons()
    {
        return addons;
    }
}
