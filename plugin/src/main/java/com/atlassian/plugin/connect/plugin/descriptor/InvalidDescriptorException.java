package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;

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

    public InvalidDescriptorException(String message, String upmMessageI18nKey)
    {
        super(message, Option.<String>some(upmMessageI18nKey));
    }

    public InvalidDescriptorException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidDescriptorException(String message, String upmMessageI18nKey, Throwable cause)
    {
        super(message, Option.<String>some(upmMessageI18nKey), cause, true);
    }
}
