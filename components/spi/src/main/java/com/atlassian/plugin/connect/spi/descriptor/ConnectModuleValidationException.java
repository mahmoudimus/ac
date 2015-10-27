package com.atlassian.plugin.connect.spi.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;

import java.io.Serializable;

/**
 * An exception thrown when syntactic or semantic validation of a descriptor module fails.
 */
public class ConnectModuleValidationException extends Exception
{

    private final ConnectModuleMeta moduleMeta;
    private final String i18nKey;
    private final Serializable[] i18nParameters;

    public ConnectModuleValidationException(ConnectModuleMeta moduleMeta, String message)
    {
        this(moduleMeta, message, null);
    }

    public ConnectModuleValidationException(ConnectModuleMeta moduleMeta, String message, String i18nKey, Serializable... i18nParameters)
    {
        super(message);
        this.moduleMeta = moduleMeta;
        this.i18nKey = i18nKey;
        this.i18nParameters = i18nParameters;
    }

    public ConnectModuleMeta getModuleMeta()
    {
        return moduleMeta;
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
