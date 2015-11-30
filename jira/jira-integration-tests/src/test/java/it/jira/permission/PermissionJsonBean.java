package it.jira.permission;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonAutoDetect
@JsonIgnoreProperties (ignoreUnknown = true)
public class PermissionJsonBean
{
    private String key;
    private String name;
    private PermissionType type;
    private String description;

    public enum PermissionType
    {
        GLOBAL, PROJECT
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public PermissionType getType()
    {
        return type;
    }

    public String getDescription()
    {
        return description;
    }
}
