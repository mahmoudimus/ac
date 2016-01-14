package com.atlassian.plugin.connect.modules.beans.nested;

/**
 * Pojo representing the format of the context returned the blueprint context url, {@link BlueprintTemplateContextBean#url}.
 *
 * The {@link #representation} is one of {@code plain}, {@code storage} or {@code wiki} only.
 * Other representations are considered invalid. By default, the representation is {@code plain}.
 *
 * The {@link #value} is the textual value for use in the context of the blueprint variable substitution. If a {@link #representation}
 * is given, the value *must* be of the correct type. For example, if representation is 'storage', then value *must* be valid
 * confluence xhtml storage format as documented here https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format .
 *
 * If the format does not match, an error will be logged and the unconverted value will be used during blueprint variable substitution.
 */
public final class BlueprintContextValue
{
    private String identifier = "";
    private String value = "";
    private String representation = "plain";

    public String getRepresentation()
    {
        return representation;
    }

    public String getValue()
    {
        return value;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public void setRepresentation(String representation)
    {
        this.representation = representation;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("BlueprintContextValue{");
        sb.append("identifier='").append(identifier).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", representation='").append(representation).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
