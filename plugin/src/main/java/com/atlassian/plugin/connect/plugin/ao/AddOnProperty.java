package com.atlassian.plugin.connect.plugin.ao;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
public final class AddOnProperty
{
    private final String key;
    private final String value;

    public AddOnProperty(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final AddOnProperty that = (AddOnProperty) o;

        if (!key.equals(that.key)) { return false; }
        if (!value.equals(that.value)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "AddOnProperty{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
