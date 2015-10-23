package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;

import java.io.Serializable;

/**
 * Thrown when an add-on fails to install due to an invalid descriptor. It is strongly recommended to use one of the
 * constructors that accepts a {@code upmMessageI18nKey} to provide useful feedback to the user.
 *
 * @since 1.0
 */
public class InvalidDescriptorException extends PluginInstallException
{
    public InvalidDescriptorException(String message)
    {
        super(message);
    }

    public InvalidDescriptorException(String message, boolean stackTraceSignificant)
    {
        super(message, stackTraceSignificant);
    }

    public InvalidDescriptorException(String message, String upmMessageI18nKey)
    {
        super(message, Option.<String>some(upmMessageI18nKey));
    }

    public InvalidDescriptorException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidDescriptorException(String message, Throwable cause, boolean stackTraceSignificant)
    {
        super(message, cause, stackTraceSignificant);
    }

    public InvalidDescriptorException(String message, Option<String> code)
    {
        super(message, code);
    }

    public InvalidDescriptorException(String message, String code, Serializable... params)
    {
        super(message, code, params);
    }

    public InvalidDescriptorException(String message, Option<String> code, boolean stackTraceSignificant)
    {
        super(message, code, stackTraceSignificant);
    }

    public InvalidDescriptorException(String message, Option<String> code, Throwable cause, boolean stackTraceSignificant)
    {
        super(message, code, cause, stackTraceSignificant);
    }

    public InvalidDescriptorException(String message, String upmMessageI18nKey, Throwable cause)
    {
        super(message, Option.<String>some(upmMessageI18nKey), cause, true);
    }
}
