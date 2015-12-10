package com.atlassian.plugin.connect.api.descriptor;

import java.io.Serializable;

/**
 * An exception thrown when JSON schema validation fails.
 */
public class ConnectJsonSchemaValidationException extends Exception
{

    private final ConnectJsonSchemaValidationResult validationResult;
    private final String i18nKey;
    private final Serializable[] i18nParameters;

    public ConnectJsonSchemaValidationException(ConnectJsonSchemaValidationResult validationResult, String message)
    {
        this(validationResult, message, null);
    }

    public ConnectJsonSchemaValidationException(ConnectJsonSchemaValidationResult validationResult, String message,
            String i18nKey, Serializable... i18nParameters)
    {
        super(message);
        this.validationResult = validationResult;
        this.i18nKey = i18nKey;
        this.i18nParameters = i18nParameters;
    }

    public ConnectJsonSchemaValidationResult getValidationResult()
    {
        return validationResult;
    }

    public String getI18nKey()
    {
        return i18nKey;
    }

    public Serializable[] getI18nParameters()
    {
        return i18nParameters;
    }
}
