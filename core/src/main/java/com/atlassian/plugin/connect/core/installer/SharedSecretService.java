package com.atlassian.plugin.connect.core.installer;

public interface SharedSecretService
{
    /**
     * Create a new shared secret that is extremely unlikely to be the same as any other.
     * @return {@link String} secret to be used for symmetric cryptography
     */
    public String next();
}
