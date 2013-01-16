package com.atlassian.plugin.remotable.descriptor;

/**
 * @since version
 */
public class InvalidDescriptorException extends RuntimeException
{
    public InvalidDescriptorException()
    {
    }

    public InvalidDescriptorException(String message)
    {
        super(message);
    }

    public InvalidDescriptorException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidDescriptorException(Throwable cause)
    {
        super(cause);
    }

}
