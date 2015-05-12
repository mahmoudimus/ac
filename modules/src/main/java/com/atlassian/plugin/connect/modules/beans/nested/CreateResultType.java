package com.atlassian.plugin.connect.modules.beans.nested;

public enum CreateResultType
{
    /**
     * Creating a page with this blueprint lands in the editor screen.
     */
    EDIT("edit"),

    /**
     * Creating a page with this blueprint lands in the view of the created page.
     */
    VIEW("view");

    private final String value;

    CreateResultType(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        return value;
    }
}
