package com.atlassian.plugin.connect.confluence.blueprint;

import com.atlassian.confluence.api.model.content.ContentRepresentation;

import static com.atlassian.confluence.api.model.content.ContentRepresentation.PLAIN;

/**
 * Pojo representing the format of the context returned from the {@link ConnectBlueprintContextProvider#contextUrl}.
 * {@link #representation} is one of {@link ContentRepresentation#PLAIN} or {@link ContentRepresentation#STORAGE} only.
 * Other representations are invalid.
 */
public final class BlueprintContextValue
{
    private String value = "";
    private String representation = PLAIN.getRepresentation();

    public ContentRepresentation getRepresentation()
    {
        return ContentRepresentation.valueOf(representation);
    }

    public String getValue()
    {
        return value;
    }
}
