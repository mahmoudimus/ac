package com.atlassian.plugin.connect.modules.beans.nested.dialog;

public enum DialogSize
{
    small,
    medium,
    large,
    xlarge("x-large"),
    fullscreen,
    // We support existing use of 'maximum' but intend for it to be replaced
    // with 'fullscreen' in a future release.
    maximum;

    private final String value;

    DialogSize()
    {
        this.value = name();
    }

    DialogSize(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
