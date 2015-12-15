package com.atlassian.plugin.connect.confluence.blueprint;

import com.atlassian.confluence.api.model.content.ContentRepresentation;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.confluence.api.model.content.ContentRepresentation.PLAIN;

/**
 * Pojo representing the format of the context returned from the {@link BlueprintContextProvider#contextUrl}.
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("representation", representation)
                .append("value", value)
                .toString();
    }
}
